package com.fast.system.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户权限缓存对象（存 Redis 的 JSON 结构）
 */
@Data
public class UserPermCacheVO {
    /** 前端动态路由树 */
    private List<RouterVo> routers = new ArrayList<>();
    /** 权限标识集合（admin 含 *:*:*） */
    private List<String> perms = new ArrayList<>();
    /** 空值标记：用户不存在时置 true，配合 60 秒短 TTL 防穿透 */
    private boolean empty;
}
