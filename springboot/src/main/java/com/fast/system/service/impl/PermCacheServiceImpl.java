package com.fast.system.service.impl;

import com.fast.system.domain.vo.RouterVo;
import com.fast.system.domain.vo.UserPermCacheVO;
import com.fast.system.mapper.UserRoleMapper;
import com.fast.system.service.IMenuService;
import com.fast.system.service.IPermCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PermCacheServiceImpl implements IPermCacheService {

    private static final String KEY_PREFIX = "perm:user:";
    private static final String LOCK_PREFIX = "lock:perm:user:";

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private IMenuService menuService;
    @Resource
    private UserRoleMapper userRoleMapper;

    @Value("${perm.cache.ttl-seconds:18000}")
    private long ttlSeconds;
    @Value("${perm.cache.ttl-random-seconds:300}")
    private long ttlRandomSeconds;
    @Value("${perm.cache.empty-ttl-seconds:60}")
    private long emptyTtlSeconds;

    @Override
    public UserPermCacheVO getOrLoad(Long userId) {
        String key = KEY_PREFIX + userId;

        // 1. 先查缓存（命中空值标记也直接返回，防穿透）
        UserPermCacheVO cached = readCache(key);
        if (cached != null) {
            return cached;
        }

        // 2. 未命中：Redisson 互斥锁重建（防击穿）
        RLock lock = redissonClient.getLock(LOCK_PREFIX + userId);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 10, TimeUnit.SECONDS); // 不等待，拿不到就走降级
            if (locked) {
                // 3. 双重检查：并发下别的线程可能已重建完成
                cached = readCache(key);
                if (cached != null) {
                    return cached;
                }
                UserPermCacheVO vo = loadFromDb(userId);
                writeCache(key, vo);
                return vo;
            }
            // 4. 未抢到锁：直接查库返回，宁可慢也不阻塞
            return loadFromDb(userId);
        } catch (Exception e) {
            log.error("权限缓存重建异常，降级查库，userId={}", userId, e);
            return loadFromDb(userId);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private UserPermCacheVO readCache(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json == null ? null : objectMapper.readValue(json, UserPermCacheVO.class);
        } catch (Exception e) {
            log.warn("读取权限缓存失败，按未命中处理，key={}", key, e);
            return null;
        }
    }

    private void writeCache(String key, UserPermCacheVO vo) {
        try {
            // 雪崩防护：正常数据 TTL = 18000 ± 300 秒随机
            long ttl = ttlSeconds + ThreadLocalRandom.current()
                    .nextLong(-ttlRandomSeconds, ttlRandomSeconds + 1);
            if (vo.isEmpty()) {
                ttl = emptyTtlSeconds; // 穿透防护：空值 60 秒
            }
            redisTemplate.opsForValue()
                    .set(key, objectMapper.writeValueAsString(vo), ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入权限缓存失败，key={}", key, e);
        }
    }

    private UserPermCacheVO loadFromDb(Long userId) {
        UserPermCacheVO vo = new UserPermCacheVO();

        // 权限标识（admin 注入通配，与 LoginController 保持一致）
        List<String> perms = menuService.selectMenuPermsByUserId(userId);
        Set<String> permSet = perms == null ? new HashSet<>() : new HashSet<>(perms);
        if (userId != null && userId == 1L) {
            permSet.add("*:*:*");
        }
        vo.setPerms(new ArrayList<>(permSet));

        // 动态路由树
        List<RouterVo> routers = menuService.selectMenuTreeRouterByUserId(userId);
        vo.setRouters(routers == null ? new ArrayList<>() : routers);

        // 用户查不到任何权限和菜单 → 视为空值（60 秒短缓存）
        vo.setEmpty(vo.getPerms().isEmpty() && vo.getRouters().isEmpty());
        return vo;
    }

    @Override
    public void put(Long userId, UserPermCacheVO vo) {
        writeCache(KEY_PREFIX + userId, vo);
    }

    @Override
    public void evictByUserId(Long userId) {
        if (userId == null) return;
        try {
            redisTemplate.delete(KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("删除用户权限缓存失败，userId={}", userId, e);
        }
    }

    @Override
    public void evictByRoleId(Long roleId) {
        if (roleId == null) return;
        // 角色变更影响所有绑定该角色的用户
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(roleId);
        if (userIds == null || userIds.isEmpty()) return;
        try {
            List<String> keys = userIds.stream().map(id -> KEY_PREFIX + id).toList();
            redisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("按角色删除权限缓存失败，roleId={}", roleId, e);
        }
    }

    @Override
    public void evictAll() {
        // 用 SCAN 不用 KEYS，避免阻塞 Redis
        try (Cursor<String> cursor = redisTemplate.scan(
                ScanOptions.scanOptions().match(KEY_PREFIX + "*").count(100).build())) {
            List<String> keys = new ArrayList<>();
            while (cursor.hasNext()) {
                keys.add(cursor.next());
                if (keys.size() >= 100) {
                    redisTemplate.delete(keys);
                    keys.clear();
                }
            }
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("清空权限缓存失败", e);
        }
    }
}
