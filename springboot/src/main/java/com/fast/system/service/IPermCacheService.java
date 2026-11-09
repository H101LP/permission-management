package com.fast.system.service;

import com.fast.system.domain.vo.UserPermCacheVO;

public interface IPermCacheService {
    /** 请求级快照 attribute key：Filter 写入，Controller 复用，避免同一请求二次查询 */
    String REQUEST_SNAPSHOT_KEY = "PERM_CACHE_SNAPSHOT";

    /** 读缓存，未命中则加锁重建（防击穿），Redis 异常自动降级查库 */
    UserPermCacheVO getOrLoad(Long userId);

    /** 直接写入缓存（登录后可用） */
    void put(Long userId, UserPermCacheVO vo);

    /** 按用户删除缓存 */
    void evictByUserId(Long userId);

    /** 按角色删除缓存：扩散到该角色下所有用户（角色变更必须用这个） */
    void evictByRoleId(Long roleId);

    /** 全量清空权限缓存（菜单变更时用），SCAN 实现 */
    void evictAll();
}
