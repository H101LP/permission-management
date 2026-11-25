package com.fast.system.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P3-1 集成测试：登录 → 鉴权 → 数据权限 → 缓存一致性 主链路（v3）。
 *
 * <p>与 v2 的差别：用例 2 / 3 / 5 全部改用夹具用户 {@code ittest_user}，
 * 不再拿真实员工"张三"做被试。这样测试不会去动 {@code login:fail:张三}、
 * {@code perm:user:5} 这些真实账号的 Redis 状态。</p>
 *
 * <p>断言口径仍然是在本机运行中的服务上实测校准过的：</p>
 * <ul>
 *   <li>业务异常（403 / 500）HTTP 状态码为 200，只有 body.code 才是真值；未认证才是真 401；</li>
 *   <li>数据权限基准一律实时查库，不写死行数；</li>
 *   <li>用例 6 用的是自建角色，不修改任何真实角色的授权。</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthAndPermissionTest extends IntegrationTestSupport {

    /**
     * 构造一个能通过 @Validated(AddGroup) 校验的探针用户。
     *
     * <p>短名不要带 case 前缀：最终用户名 = {@code ittest_case} + 短名，
     * 例如传 {@code "4"} 得到 {@code ittest_case4}。命名必须和基类
     * {@code cleanTempUsers()} 的 pattern {@code ittest_case%} 对齐，否则清不掉。</p>
     *
     * <p>roleId 用夹具角色，不再绑真实 role2。</p>
     *
     * <p>deptId 已在 UserMapper.xml 修复（insertUser / updateUser 都会写 dept_id 列），
     * 用例 4 会断言它确实落库。</p>
     */
    private Map<String, Object> probeUser(String suffix) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userName", TEMP_USER_PREFIX + suffix);
        user.put("password", "123456");
        user.put("sex", 0);
        user.put("deptId", FIXTURE_DEPT_ID);
        user.put("roleId", fixtureRoleId);
        user.put("status", "0");
        return user;
    }

    /** updateRole 的请求体：roleName / roleSort / dataScope 都传夹具的当前值，避免多余改写 */
    private Map<String, Object> roleBody(List<Long> menuIds) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("roleId", fixtureRoleId);
        body.put("roleName", FIXTURE_ROLE);
        body.put("roleSort", 99);
        body.put("dataScope", 2);
        body.put("menuIds", menuIds);
        return body;
    }

    // ==================================================================
    @Test
    @Order(1)
    @DisplayName("用例1：正确密码登录 → code=200，token 非空，超管权限集含 *:*:*")
    void case1_loginSuccess() throws Exception {
        JsonNode node = loginNode(ADMIN, ADMIN_PWD);

        assertEquals(200, node.path("code").asInt(), "响应：" + node);

        String token = node.path("data").path("token").asText();
        assertFalse(token.isBlank(), "token 为空");
        // 长度只是廉价自检，不作为"JWT 已签发"的证据；真正的证据是用它访问受保护接口（见用例 5）
        assertTrue(token.length() > 200, "token 长度异常：" + token.length());

        assertTrue(containsPerm(node.path("data").path("permissions"), "*:*:*"),
                "超管权限集应含 *:*:*，实际：" + node.path("data").path("permissions"));
    }

    // ==================================================================
    @Test
    @Order(2)
    @DisplayName("用例2：错误密码 → code=500，msg 含“密码错误”，失败计数为 1")
    void case2_loginWrongPassword() throws Exception {
        // 用夹具用户而不是真实员工：这一轮只会在 login:fail:ittest_user 上计数，
        // 不会碰真实账号的锁定状态
        JsonNode node = loginNode(FIXTURE_USER, "wrong-password-for-it");

        // BusinessException("密码错误") 走 GlobalExceptionHandler，HTTP 仍是 200
        assertEquals(500, node.path("code").asInt(), "响应：" + node);
        assertTrue(node.path("msg").asText().contains("密码错误"),
                "msg 不符合预期：" + node.path("msg").asText());

        // recordFail 首次失败写 "1"；这一条同时说明：锁定逻辑在计数，且计数没有跨用例累积
        assertEquals("1", redis.opsForValue().get("login:fail:" + FIXTURE_USER),
                "失败计数异常：要么没清理导致累积（第 5 轮会锁号）、要么锁定逻辑没生效");
    }

    // ==================================================================
    @Test
    @Order(3)
    @DisplayName("用例3：普通用户调写接口 → code=403，且没有产生可见写入")
    void case3_employeeCannotWrite() throws Exception {
        String token = tokenOf(FIXTURE_USER, FIXTURE_PWD);

        JsonNode node = postJson("/system/user/insertUser", token, probeUser("3"));

        assertEquals(403, node.path("code").asInt(), "响应：" + node);
        assertEquals("权限不足，无法访问", node.path("msg").asText());

        // 只表述为"没有产生可见写入"。
        // 不要写成"证明切面在 Service 之前"：UserServiceImpl.insertUser 带 @Transactional，
        // 即使异常发生在 Service 内部，回滚后同样是 0 条。
        assertEquals(0, countRows("SELECT COUNT(*) FROM user WHERE user_name = ?",
                        TEMP_USER_PREFIX + "3"),
                "返回 403 却产生了可见写入，鉴权链路异常");
    }

    // ==================================================================
    @Test
    @Order(4)
    @DisplayName("用例4：admin 调同一写接口 → code=200，落库且密码为 BCrypt 密文")
    void case4_adminCanWrite() throws Exception {
        String token = tokenOf(ADMIN, ADMIN_PWD);

        JsonNode node = postJson("/system/user/insertUser", token, probeUser("4"));

        assertEquals(200, node.path("code").asInt(), "响应：" + node);
        assertEquals(1, countRows("SELECT COUNT(*) FROM user WHERE user_name = ?",
                TEMP_USER_PREFIX + "4"), "admin 调写接口应落库");

        // 回归 P0-2：新增用户链路的密码必须是密文，且确实是 "123456" 的 BCrypt 结果
        String stored = jdbc.queryForObject("SELECT password FROM user WHERE user_name = ?",
                String.class, TEMP_USER_PREFIX + "4");
        assertNotNull(stored);
        assertTrue(stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"),
                "密码不是 BCrypt 密文：" + stored);
        assertFalse(stored.contains("123456"), "密码里出现了明文");
        assertTrue(passwordEncoder.matches("123456", stored), "密文无法还原成原密码");

        // user_role 关联也应建立（挂的是夹具角色，不是真实 role2）
        assertEquals(1, countRows("SELECT COUNT(*) FROM user_role ur JOIN user u ON u.user_id = ur.user_id "
                        + "WHERE u.user_name = ? AND ur.role_id = ?", TEMP_USER_PREFIX + "4", fixtureRoleId),
                "新增用户未写入 user_role");

        // 回归 dept_id 缺陷（已在 UserMapper.xml 修复）：新增用户必须把所选部门落库
        assertEquals(FIXTURE_DEPT_ID,
                jdbc.queryForObject("SELECT dept_id FROM user WHERE user_name = ?",
                        Long.class, TEMP_USER_PREFIX + "4").longValue(),
                "新增用户未写入 dept_id");
    }

    // ==================================================================
    @Test
    @Order(5)
    @DisplayName("用例5：普通用户查用户列表 → total/rows 都只剩本部门，admin 对照全量")
    void case5_dataScopeOnList() throws Exception {
        // 基准不写死，直接查当前库：任何增删用户都不会让这条测试失效
        int expectedDept4 = countRows("SELECT COUNT(*) FROM user WHERE dept_id = ?", FIXTURE_DEPT_ID);
        int expectedAll = countRows("SELECT COUNT(*) FROM user");

        // 夹具用户 dept=4、role 数据范围=2（本部门）
        String empToken = tokenOf(FIXTURE_USER, FIXTURE_PWD);
        JsonNode node = getJson("/system/user/selectUserList?pageNum=1&pageSize=" + (expectedAll + 10), empToken);

        assertEquals(200, node.path("code").asInt(), "响应：" + node);
        JsonNode data = node.path("data");

        assertEquals(expectedDept4, data.path("total").asInt(),
                "total 未按数据权限过滤（PageHelper 的 _COUNT 漏了？）");
        assertEquals(expectedDept4, data.path("rows").size(), "rows 数量不对");
        for (JsonNode row : data.path("rows")) {
            assertEquals(FIXTURE_DEPT_ID, row.path("deptId").asInt(),
                    "越权返回了其他部门的用户：" + row.path("userName").asText());
        }

        // 反向对照：admin 必须是全量，否则无法证明上面的数字是"过滤"出来的
        String adminToken = tokenOf(ADMIN, ADMIN_PWD);
        JsonNode all = getJson("/system/user/selectUserList?pageNum=1&pageSize=" + (expectedAll + 10), adminToken);
        assertEquals(expectedAll, all.path("data").path("total").asInt(), "admin 应为全量");
        assertEquals(expectedAll, all.path("data").path("rows").size(), "admin rows 应为全量");
    }

    // ==================================================================
    @Test
    @Order(6)
    @DisplayName("用例6：改角色后不重登 → 同一旧 token 授权生效、收权也立即生效")
    void case6_permissionRefreshWithoutRelogin() throws Exception {
        // 0. 夹具用户登录（role=ittest_role，只挂 menu 1、2，按钮权限为空）
        String oldToken = tokenOf(FIXTURE_USER, FIXTURE_PWD);
        String adminToken = tokenOf(ADMIN, ADMIN_PWD);
        String cacheKey = "perm:user:" + fixtureUserId;

        // 1. 基线：无 system:user:add，必须 403
        JsonNode before = postJson("/system/user/insertUser", oldToken, probeUser("6_before"));
        assertEquals(403, before.path("code").asInt(), "基线就应为 403：" + before);

        // 2. 上一次请求已让过滤器建立过权限缓存，先确认它存在，否则后面的失效断言等于没测
        assertNotNull(redis.opsForValue().get(cacheKey), "权限缓存未建立，后续断言无意义");

        // 3. 授权：admin 给夹具角色补上 menu 6（perms = system:user:add）
        JsonNode grant = postJson("/system/role/updateRole", adminToken, roleBody(List.of(1L, 2L, 6L)));
        assertEquals(200, grant.path("code").asInt(), "授权失败：" + grant);
        awaitKeyDeleted(cacheKey, 2000L);

        // 4. 关键：仍然用 oldToken，不重新登录，写接口应被放行
        JsonNode afterGrant = postJson("/system/user/insertUser", oldToken, probeUser("6_granted"));
        assertEquals(200, afterGrant.path("code").asInt(),
                "授权后旧 token 权限没刷新，说明 evictByRoleId 没生效：" + afterGrant);
        assertEquals(1, countRows("SELECT COUNT(*) FROM user WHERE user_name = ?",
                TEMP_USER_PREFIX + "6_granted"), "放行后应真的写入");

        // 5. 收权：去掉 menu 6，恢复到基线
        JsonNode revoke = postJson("/system/role/updateRole", adminToken, roleBody(List.of(1L, 2L)));
        assertEquals(200, revoke.path("code").asInt(), "收权失败：" + revoke);
        awaitKeyDeleted(cacheKey, 2000L);

        // 6. 同一 oldToken 再打一次：权限被收回必须立刻拒绝。
        //    这是权限系统更重要的性质——"收回即时生效"，而不只是"授予即时生效"。
        JsonNode afterRevoke = postJson("/system/user/insertUser", oldToken, probeUser("6_revoked"));
        assertEquals(403, afterRevoke.path("code").asInt(),
                "收权后旧 token 仍然能写，权限刷新只做了单向：" + afterRevoke);
        assertEquals(0, countRows("SELECT COUNT(*) FROM user WHERE user_name = ?",
                TEMP_USER_PREFIX + "6_revoked"), "被拒绝的请求不应产生写入");
    }

    // ==================================================================
    /**
     * 等缓存键被删除，超时就判失败。
     * <p>注意：这只能证明"键最终消失了"，证明不了"它是在事务提交之后才消失"。
     * 要验证 afterCommit 的提交时序，需要"改角色后强制回滚、断言缓存未删"的专门用例，
     * 那需要能干预业务事务的手段，不在本套 6 个用例的范围内。</p>
     */
    private void awaitKeyDeleted(String key, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (redis.opsForValue().get(key) == null) {
                return;
            }
            Thread.sleep(50L);
        }
        assertNull(redis.opsForValue().get(key),
                "缓存键 " + key + " 未被清除，evictByRoleId 未生效");
    }
}
