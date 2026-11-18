package com.fast.system.controller;

import com.fast.system.configure.TokenBlacklistService;
import com.fast.system.configure.TokenService;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginBody;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.User;
import com.fast.system.exception.BusinessException;
import com.fast.system.mapper.MenuMapper;
import com.fast.system.service.IPermCacheService;
import com.fast.system.service.IUserService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;                          // @Slf4j
import org.springframework.beans.factory.annotation.Value; // @Value
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;                        // TimeUnit

import java.util.*;

/**
 * 登录控制器
 */
@Slf4j
@RestController
public class LoginController extends BaseController {
    @Resource
    private IUserService userService;
    @Resource
    private TokenService tokenService;
    @Resource
    private MenuMapper menuMapper;
    @Resource
    private IPermCacheService permCacheService;
    @Resource
    private PasswordEncoder passwordEncoder;
    /** token 黑名单（登出主动失效） */
    @Resource
    private TokenBlacklistService tokenBlacklistService;
    /*
     * Redis 操作模板
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 连续失败多少次后锁定，默认 5 */
    @Value("${login.fail.max-count:5}")
    private int maxFailCount;

    /** 锁定分钟数，默认 15 */
    @Value("${login.fail.lock-minutes:15}")
    private int lockMinutes;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public AjaxResult login(@Validated @RequestBody LoginBody loginBody){
        //2验证用户是否存在
        User user = userService.selectByUserName(loginBody.getUserName());
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        // 失败锁定判断
        // 用 DB 里的规范用户名做 key，避免 Admin / admin 各算一套额度
        String failKey = "login:fail:" + user.getUserName();
        int failCount = readFailCount(failKey);
        if (failCount >= maxFailCount) {
            throw new BusinessException("密码错误次数过多，账号已锁定 " + lockMinutes + " 分钟");
        }
        //验证密码是否正确
        String rawPwd = loginBody.getPassword();
        String storedPwd = user.getPassword();

        if (!passwordEncoder.matches(rawPwd, storedPwd)) {
            // 兼容存量明文（迁移期）：明文相等则放行，并顺手改写成密文
            if (rawPwd.equals(storedPwd)) {
                // resetUserPwd 内部会 encode 一次，这里必须传明文，否则会二次加密导致下次登录永久失败
                userService.resetUserPwd(user.getUserId(), rawPwd);
            } else {
                // 记一次失败（固定 15 分钟窗口）
                recordFail(failKey);
                throw new BusinessException("密码错误");
            }
        }
        // 密码正确，清零失败计数
        clearFail(failKey);
        // 密码校验通过后判状态：避免"密码错的人也能探测账号是否被停用"
        if ("1".equals(user.getStatus())) {
            throw new BusinessException("账号已停用，请联系管理员");
        }
        LoginUser loginUser = new LoginUser(user.getUserId(),user);
        //查询用户的所有权限
        List<String> permsList = menuMapper.selectMenuPermsByUserId(user.getUserId());
        //转为Set
        Set<String> permsSet = new HashSet<>();
        if (user.getUserId() != null && user.getUserId() == 1L) {
            permsSet.add("*:*:*");
        }else if(permsList != null && !permsList.isEmpty()){
            permsSet.addAll(permsList);
        }

        //装载到LoginUser中
        loginUser.setPermissions(permsSet);
        //将用户部门ID装入LoginUser中
        loginUser.setDeptId(user.getDeptId());
        //判断是否为管理员级别
        loginUser.setAdmin(user.getUserId() != null && user.getUserId() == 1L);

        permCacheService.evictByUserId(user.getUserId());

        //生成JWT令牌
        String token = tokenService.createToken(loginUser);
        //返回成功结果 包含token
        Map<String, Object> map = new HashMap<>();
        map.put("permissions", permsSet);
        map.put("token", token); return success(map);

    }
    /**
     * 登出接口
     *
     * 设计要点：
     * 1. 幂等 —— 没带 token、token 已过期、token 已拉黑，一律返回成功，
     *    绝不返回 401（否则前端会弹"登录已过期"，退出流程死循环）
     * 2. 只拉黑"这一张"令牌 —— 黑名单粒度是 jti 不是 userId，
     *    所以 A 设备登出不会踢掉 B 设备
     * 3. 返回前不清理任何服务端缓存 —— 权限缓存下次登录时 login() 里会 evict
     */
    @PostMapping("/logout")
    public AjaxResult logout(HttpServletRequest request) {
        String token = tokenService.getToken(request);
        // 没带 token：视为已经退出过，直接成功
        if (!StringUtils.hasText(token)) {
            return success();
        }
        try {
            Claims claims = tokenService.parseToken(token);
            String jti = claims.get("jti", String.class);
            // TTL = 该令牌剩余寿命，过期即自动从 Redis 消失
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            tokenBlacklistService.add(jti, ttl);
        } catch (Exception e) {
            // token 已过期 / 签名不对 / 格式损坏：它本来就已经不可用了，无需拉黑
            // 这里必须吞掉异常，保证 /logout 永远 200
            log.info("登出时解析 token 失败，按已失效处理：{}", e.getMessage());
        }
        return success();
    }

    /**
     * 读取失败次数。
     * Redis 不可用时降级为 0：宁可放过一次爆破，也不能让全站登录不可用。
     */
    private int readFailCount(String failKey) {
        try {
            String value = stringRedisTemplate.opsForValue().get(failKey);
            if (value == null || value.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (Exception e) {
            log.warn("读取登录失败计数异常，降级为 0，key={}", failKey, e);
            return 0;
        }
    }

    /**
     * 记录一次密码错误。
     * 首次失败用 SET NX EX 原子创建（自带 TTL），后续只累加、不重置 TTL —— 固定窗口：
     * 从第一次失败起算 15 分钟，不随重试延长，避免"一直被试就一直锁着"。
     *
     * 注意：不要改回 stringRedisTemplate.expire(...)。当前依赖组合（spring-data-redis 3.4.5）
     * 下 expire 会走到 DefaultedRedisConnection.pExpire 的自递归，抛 StackOverflowError；
     * StackOverflowError 属于 Error 而非 Exception，catch(Exception) 抓不住，会直接冒到
     * 全局兜底变成"系统繁忙，请稍后重试"，同时 key 因为没有 TTL 会永久留存。
     */
    private void recordFail(String failKey) {
        try {
            // 首次失败：SET key 1 NX EX 900，一条原子命令同时完成"创建 + 设 TTL"，不经过 pExpire
            Boolean created = stringRedisTemplate.opsForValue()
                    .setIfAbsent(failKey, "1", lockMinutes, TimeUnit.MINUTES);
            // 已存在：只累加。Redis 的 INCR 只改 value，原 TTL 保持不变，固定窗口语义不变
            if (!Boolean.TRUE.equals(created)) {
                stringRedisTemplate.opsForValue().increment(failKey);
            }
        } catch (Exception e) {
            log.warn("写入登录失败计数异常，忽略本次计数，key={}", failKey, e);
        }
    }

    /**
     * 登录成功后清零。
     */
    private void clearFail(String failKey) {
        try {
            stringRedisTemplate.delete(failKey);
        } catch (Exception e) {
            log.warn("清除登录失败计数异常，key={}", failKey, e);
        }
    }


}
