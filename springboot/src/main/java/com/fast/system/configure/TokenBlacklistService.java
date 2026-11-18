package com.fast.system.configure;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * token 黑名单（登出主动失效）
 *
 * 背景：JWT 是无状态的，服务端手上没有"谁已登出"的记录，只能等令牌自然过期。
 * 想做"退出即刻失效"，就必须额外存一份作废名单 —— 这里用 Redis。
 *
 * key 设计：token:blacklist:{jti}
 * TTL 设计：精确等于该令牌的剩余寿命。令牌自己过期后就不需要再拦了，
 *          所以黑名单天然自清理，数量上限 ≈ "有效期内的登出次数"，永远不会无限增长。
 */
@Slf4j
@Component
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "token:blacklist:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 拉黑一个 jti。
     *
     * @param jti       令牌唯一标识
     * @param ttlMillis 剩余存活毫秒数；<= 0 表示已过期，无需拉黑
     */
    public void add(String jti, long ttlMillis) {
        if (jti == null || ttlMillis <= 0) {
            return;
        }
        try {
            // 用 set(key, value, timeout, unit)：底层是 SET key value PX ttl，一条命令完成"写入+过期"
            // 千万不要改成 expire(key, ttl, unit)：spring-data-redis 3.4.5 下它会走到
            // DefaultedRedisConnection.pExpire 的自递归，抛 StackOverflowError
            stringRedisTemplate.opsForValue()
                    .set(KEY_PREFIX + jti, "1", ttlMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // 写黑名单失败就失败：登出请求仍然返回成功，总比整个退出流程卡住好
            log.warn("写入 token 黑名单失败，jti={}", jti, e);
        }
    }

    /**
     * 是否已被拉黑。
     */
    public boolean contains(String jti) {
        if (jti == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(KEY_PREFIX + jti));
        } catch (Exception e) {
            // 降级放行：Redis 单次抖动时，宁可放过一个"可能已登出"的令牌，
            // 也不要因为查不到黑名单就把所有人的请求都判成 401
            log.warn("查询 token 黑名单失败，本次放行，jti={}", jti, e);
            return false;
        }
    }
}
