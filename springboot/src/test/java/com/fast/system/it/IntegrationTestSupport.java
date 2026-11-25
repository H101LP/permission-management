package com.fast.system.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P3-1 集成测试基类（v3）。
 *
 * <p>相对 v2 的修正，全部来自第二轮复审：</p>
 * <ol>
 *   <li><b>日志清理不再看操作人名。</b> v2 用 {@code user_name IN ('admin','ittest_user','张三')}，
 *       会误删同一时间窗内真实 admin 的操作日志。现在只认 {@code request_param} 里的测试标识
 *       （{@code ittest_case*} / {@code ittest_role}），跟谁操作的无关。</li>
 *   <li><b>异步日志改成确定性轮询。</b> v2 是固定 {@code Thread.sleep(1500)}，
 *       线程池拥堵时日志可能晚于清理落库，留下再也删不掉的记录。
 *       现在循环"删 → 复查 → 连续 3 次为 0 才算干净"，最多等 3 秒。</li>
 *   <li><b>不再碰真实用户的 Redis。</b> 用例 2/3/5 改用夹具用户，
 *       {@code login:fail:张三}、{@code perm:user:5} 一律不再删，只清夹具自己的 key。</li>
 *   <li><b>夹具创建半途失败也能收尾。</b> {@code @BeforeAll} 包 try/catch，
 *       失败走 best-effort 清理再抛出（JUnit 对 @BeforeAll 失败后是否调 @AfterAll 不作保证）。</li>
 *   <li><b>清理结果纳入测试自身断言。</b> {@code @AfterAll} 结束前断言探针用户为 0、
 *       夹具角色为 0、缓存键不存在 —— 清理失败要让测试红，而不是靠人肉查库。</li>
 * </ol>
 *
 * <p>仍存在的限制（已写进 README）：固定夹具名不支持两个进程并行；
 * 测试验证的是"最终一致"，不是 {@code afterCommit} 的提交时序。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestSupport {

    /** 夹具角色名（@BeforeAll 建、@AfterAll 删，全程不碰真实角色） */
    protected static final String FIXTURE_ROLE = "ittest_role";
    /** 夹具用户名 */
    protected static final String FIXTURE_USER = "ittest_user";
    protected static final String FIXTURE_PWD = "123456";

    /** 各用例临时插入的用户前缀 */
    protected static final String TEMP_USER_PREFIX = "ittest_case";

    protected static final String ADMIN = "admin";
    protected static final String ADMIN_PWD = "admin123";

    /** 夹具用户所在部门：前端组（不是 admin 所在的总经办，便于对照数据权限） */
    protected static final long FIXTURE_DEPT_ID = 4L;

    // 审计日志清理条件：只认"请求参数里带测试标识"的记录。
    // insertUser 的 body 里有 "userName":"ittest_case4"，updateRole 的 body 里有 "roleName":"ittest_role"。
    // 这样即使同一时刻有真人用 admin 操作，他的日志也不会被误删。
    private static final String OP_LOG_COND =
            "log_id > ? AND request_uri IN (?, ?) AND (request_param LIKE ? OR request_param LIKE ?)";
    private static final String OP_LOG_URI_INSERT = "/system/user/insertUser";
    private static final String OP_LOG_URI_UPDATE_ROLE = "/system/role/updateRole";
    private static final String OP_LOG_PARAM_TEMP_USER = "%ittest\\_case%";
    private static final String OP_LOG_PARAM_FIXTURE_ROLE = "%ittest\\_role%";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper om;

    @Autowired
    protected StringRedisTemplate redis;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Long fixtureRoleId;
    protected Long fixtureUserId;

    protected long opLogBaseline;
    protected long permLogBaseline;

    // ==================== 类级夹具 ====================

    @BeforeAll
    void createFixture() {
        try {
            doCreateFixture();
        } catch (Throwable t) {
            // JUnit 不保证 @BeforeAll 失败后还执行 @AfterAll，这里先尽力收尾再抛
            safeCleanupSilently();
            throw t;
        }
    }

    private void doCreateFixture() {
        cleanTempUsers();
        deleteFixtureUser();
        deleteFixtureRole();

        opLogBaseline = maxLogId("sys_operation_log");
        permLogBaseline = maxLogId("sys_permission_log");

        // 专用角色：只挂 menu 1（系统管理，M）和 menu 2（用户管理，C）。
        // 这两个节点 perms 为空，所以该角色登录后 permissions = []，任何按钮权限都没有。
        jdbc.update("INSERT INTO role(role_name, role_sort, data_scope) VALUES (?, ?, ?)",
                FIXTURE_ROLE, 99, 2);
        fixtureRoleId = jdbc.queryForObject(
                "SELECT role_id FROM role WHERE role_name = ?", Long.class, FIXTURE_ROLE);
        jdbc.update("INSERT INTO role_menu(role_id, menu_id) VALUES (?, 1), (?, 2)",
                fixtureRoleId, fixtureRoleId);

        // 专用用户：dept_id=4，密码 123456
        jdbc.update("INSERT INTO user(user_name, sex, password, dept_id, status) VALUES (?, ?, ?, ?, ?)",
                FIXTURE_USER, 0, passwordEncoder.encode(FIXTURE_PWD), FIXTURE_DEPT_ID, "0");
        fixtureUserId = jdbc.queryForObject(
                "SELECT user_id FROM user WHERE user_name = ?", Long.class, FIXTURE_USER);
        jdbc.update("INSERT INTO user_role(user_id, role_id) VALUES (?, ?)",
                fixtureUserId, fixtureRoleId);
    }

    @AfterAll
    void destroyFixture() throws Exception {
        cleanTempUsers();
        resetFixtureMenus();

        // 异步日志：循环"删 → 复查"，连续 3 次为 0 才认为落库已稳定，最多等 3 秒
        purgeTestAuditLogs(3000L);

        deleteFixtureUser();
        deleteFixtureRole();

        if (fixtureUserId != null) {
            redis.delete(List.of("login:fail:" + FIXTURE_USER, "perm:user:" + fixtureUserId));
        }

        // 清理结果自检：失败就让测试红，而不是靠人肉跑 SQL 复查
        assertEquals(0, countRows("SELECT COUNT(*) FROM user WHERE user_name LIKE ?", "ittest\\_%"),
                "清理失败：库里仍有 ittest 探针用户");
        assertEquals(0, countRows("SELECT COUNT(*) FROM role WHERE role_name = ?", FIXTURE_ROLE),
                "清理失败：夹具角色未被删除");
        assertEquals(0, countAuditLogs(), "清理失败：审计表仍有本次测试产生的记录");
        if (fixtureUserId != null) {
            assertNull(redis.opsForValue().get("perm:user:" + fixtureUserId),
                    "清理失败：夹具用户的权限缓存未删除");
        }
    }

    /** 只用名字定位，不依赖 fixtureRoleId / fixtureUserId 是否已赋值 */
    private void safeCleanupSilently() {
        try {
            cleanTempUsers();
        } catch (Throwable ignore) {
            // best effort
        }
        try {
            deleteFixtureUser();
        } catch (Throwable ignore) {
            // best effort
        }
        try {
            deleteFixtureRole();
        } catch (Throwable ignore) {
            // best effort
        }
    }

    // ==================== 用例级重置 ====================

    @BeforeEach
    void resetBeforeEach() {
        cleanTempUsers();
        resetFixtureMenus();
        clearFixtureCacheKeys();
    }

    @AfterEach
    void resetAfterEach() {
        cleanTempUsers();
        resetFixtureMenus();
        clearFixtureCacheKeys();
    }

    /** 把夹具角色的菜单无条件重置成基线 [1,2] —— 基线是自建的，不需要猜 */
    protected void resetFixtureMenus() {
        if (fixtureRoleId == null) {
            return;
        }
        jdbc.update("DELETE FROM role_menu WHERE role_id = ?", fixtureRoleId);
        jdbc.update("INSERT INTO role_menu(role_id, menu_id) VALUES (?, 1), (?, 2)",
                fixtureRoleId, fixtureRoleId);
    }

    /**
     * 只清临时用户，不动夹具用户。
     * <p>命名约定：临时用户一律是 {@code ittest_case} + 短名；夹具用户是 {@code ittest_user}，
     * 前缀不同，不会被这里误删。pattern 里转义了下划线，避免 {@code _} 被当成 LIKE 通配符。</p>
     */
    protected void cleanTempUsers() {
        String pattern = "ittest\\_case%";
        jdbc.update("DELETE FROM user_role WHERE user_id IN "
                + "(SELECT user_id FROM user WHERE user_name LIKE ?)", pattern);
        jdbc.update("DELETE FROM user WHERE user_name LIKE ?", pattern);
    }

    /**
     * 只清夹具自己的缓存键。
     * <p>v2 会删 {@code login:fail:张三}、{@code perm:user:5}、{@code perm:user:1}，
     * 那是真实账号的状态，多人共用开发库时会互相干扰。现在用例都改用夹具用户，
     * admin 的缓存也不清（admin 权限不变，清了只是多一次重建，没必要）。</p>
     */
    private void clearFixtureCacheKeys() {
        if (fixtureUserId == null) {
            return;
        }
        redis.delete(List.of("login:fail:" + FIXTURE_USER, "perm:user:" + fixtureUserId));
    }

    private void deleteFixtureUser() {
        jdbc.update("DELETE FROM user_role WHERE user_id IN "
                + "(SELECT user_id FROM user WHERE user_name = ?)", FIXTURE_USER);
        jdbc.update("DELETE FROM user WHERE user_name = ?", FIXTURE_USER);
    }

    private void deleteFixtureRole() {
        jdbc.update("DELETE FROM role_menu WHERE role_id IN "
                + "(SELECT role_id FROM role WHERE role_name = ?)", FIXTURE_ROLE);
        jdbc.update("DELETE FROM role WHERE role_name = ?", FIXTURE_ROLE);
    }

    /** 表名来自本类常量，不接受外部输入 */
    private long maxLogId(String table) {
        Long value = jdbc.queryForObject("SELECT COALESCE(MAX(log_id), 0) FROM " + table, Long.class);
        return value == null ? 0L : value;
    }

    // ==================== 审计日志清理 ====================

    private void deleteTestAuditLogs() {
        jdbc.update("DELETE FROM sys_operation_log WHERE " + OP_LOG_COND,
                opLogBaseline, OP_LOG_URI_INSERT, OP_LOG_URI_UPDATE_ROLE,
                OP_LOG_PARAM_TEMP_USER, OP_LOG_PARAM_FIXTURE_ROLE);
        if (fixtureRoleId != null) {
            jdbc.update("DELETE FROM sys_permission_log WHERE log_id > ? AND target_role_id = ?",
                    permLogBaseline, fixtureRoleId);
        }
    }

    private int countAuditLogs() {
        int op = countRows("SELECT COUNT(*) FROM sys_operation_log WHERE " + OP_LOG_COND,
                opLogBaseline, OP_LOG_URI_INSERT, OP_LOG_URI_UPDATE_ROLE,
                OP_LOG_PARAM_TEMP_USER, OP_LOG_PARAM_FIXTURE_ROLE);
        int perm = 0;
        if (fixtureRoleId != null) {
            perm = countRows("SELECT COUNT(*) FROM sys_permission_log WHERE log_id > ? AND target_role_id = ?",
                    permLogBaseline, fixtureRoleId);
        }
        return op + perm;
    }

    /**
     * 异步日志可能比 HTTP 响应晚落库，固定 sleep 不可靠。
     * 这里反复"删 → 复查"，连续 3 次（约 300ms）为零才收手。
     */
    private void purgeTestAuditLogs(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        int zeroStreak = 0;
        while (System.currentTimeMillis() < deadline) {
            if (countAuditLogs() > 0) {
                deleteTestAuditLogs();
                zeroStreak = 0;
            } else {
                zeroStreak++;
                if (zeroStreak >= 3) {
                    return;
                }
            }
            Thread.sleep(100L);
        }
    }

    // ==================== 请求辅助 ====================

    protected JsonNode loginNode(String userName, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "userName", userName,
                                "password", password))))
                .andReturn();
        return readJson(result);
    }

    protected String tokenOf(String userName, String password) throws Exception {
        JsonNode node = loginNode(userName, password);
        assertEquals(200, node.path("code").asInt(), "登录失败：" + node);
        String token = node.path("data").path("token").asText();
        assertFalse(token.isBlank(), "登录成功但 token 为空");
        return token;
    }

    protected JsonNode getJson(String path, String token) throws Exception {
        MvcResult result = mockMvc.perform(get(path)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result);
    }

    /**
     * POST JSON，要求 HTTP 200。
     * <p>业务异常（403 / 500）经 GlobalExceptionHandler 统一返回，HTTP 状态码仍是 200，
     * 真正的结果在 body.code 里 —— 这里不能断言 status().isForbidden()。</p>
     */
    protected JsonNode postJson(String path, String token, Object body) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result);
    }

    /**
     * PUT JSON，要求 HTTP 200。
     * <p>updateUser 这类接口是 PUT，用 POST 打会 405。</p>
     */
    protected JsonNode putJson(String path, String token, Object body) throws Exception {
        MvcResult result = mockMvc.perform(put(path)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result);
    }

    protected JsonNode readJson(MvcResult result) throws Exception {
        String text = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertNotNull(text, "响应体为空");
        return om.readTree(text);
    }

    // ==================== 断言辅助 ====================

    protected boolean containsPerm(JsonNode permsArray, String perm) {
        if (permsArray == null || !permsArray.isArray()) {
            return false;
        }
        for (JsonNode item : permsArray) {
            if (perm.equals(item.asText())) {
                return true;
            }
        }
        return false;
    }

    protected int countRows(String sql, Object... args) {
        Integer count = jdbc.queryForObject(sql, Integer.class, args);
        return count == null ? 0 : count;
    }
}
