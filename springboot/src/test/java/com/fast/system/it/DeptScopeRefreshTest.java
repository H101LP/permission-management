package com.fast.system.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 回归「改部门后旧 token 的数据权限不刷新」。
 *
 * <p>之前的实现：{@code LoginUser.deptId} 只在登录那一刻从 user 表读，之后被序列化进 JWT。
 * {@code JwtAuthenticationTokenFilter} 每次请求只把 {@code permissions} 用缓存覆盖，
 * {@code deptId} / {@code dataScope} 一直是签发时的快照。
 * 结果：管理员把员工从部门 4 改到部门 5，数据库改了，但该员工旧 token 仍按部门 4 过滤。</p>
 *
 * <p>修复后：缓存 VO 里带上 deptId / dataScope，过滤器一并覆盖；
 * {@code updateUser} 本来就会 {@code evictByUserId}，所以改完下一个请求就生效。</p>
 *
 * <p>本用例的关键在于<b>全程使用改部门之前签发的那一张 token</b>，中间不重新登录。</p>
 */
class DeptScopeRefreshTest extends IntegrationTestSupport {

    @Test
    @DisplayName("改部门后不重登：旧 token 的数据范围立即切到新部门")
    void changingDeptRefreshesOldTokenDataScope() throws Exception {
        // 1. 夹具用户登录（建在 dept=4，角色数据范围=2 本部门）
        String oldToken = tokenOf(FIXTURE_USER, FIXTURE_PWD);

        int dept4 = countRows("SELECT COUNT(*) FROM user WHERE dept_id = 4");
        JsonNode before = getJson("/system/user/selectUserList?pageNum=1&pageSize=" + (dept4 + 10), oldToken);
        assertEquals(200, before.path("code").asInt(), "响应：" + before);
        assertEquals(dept4, before.path("data").path("total").asInt(),
                "改部门前应只看到 dept4 的人");

        // 2. admin 把夹具用户改到 dept=5
        String adminToken = tokenOf(ADMIN, ADMIN_PWD);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", fixtureUserId);
        body.put("userName", FIXTURE_USER);
        body.put("deptId", 5L);
        JsonNode upd = putJson("/system/user/updateUser", adminToken, body);
        assertEquals(200, upd.path("code").asInt(), "改部门失败：" + upd);
        assertEquals(5L,
                jdbc.queryForObject("SELECT dept_id FROM user WHERE user_id = ?",
                        Long.class, fixtureUserId).longValue(),
                "数据库里 dept_id 没改过来");

        // 3. 关键：仍然用 oldToken，不重新登录
        int dept5 = countRows("SELECT COUNT(*) FROM user WHERE dept_id = 5");
        JsonNode after = getJson("/system/user/selectUserList?pageNum=1&pageSize=" + (dept5 + 10), oldToken);
        assertEquals(dept5, after.path("data").path("total").asInt(),
                "改部门后旧 token 仍按旧部门过滤 —— deptId 没有从缓存刷新");
        for (JsonNode row : after.path("data").path("rows")) {
            assertEquals(5, row.path("deptId").asInt(),
                    "越权返回了其他部门的用户：" + row.path("userName").asText());
        }
    }
}
