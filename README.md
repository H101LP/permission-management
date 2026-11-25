# RBAC 多层级权限管理平台

**基于 Spring Boot 3 与 Vue 3 的企业管理平台，贯通菜单、按钮、接口与行级数据权限，并提供权限缓存一致性、操作审计、业务数据隔离和统一管理界面。**

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?style=flat-square&logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.5.40-4FC08D?style=flat-square&logo=vuedotjs)
![Vite](https://img.shields.io/badge/Vite-8.1.5-646CFF?style=flat-square&logo=vite)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-5.0%2B-DC382D?style=flat-square&logo=redis)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?style=flat-square&logo=springsecurity)
![JDK](https://img.shields.io/badge/JDK-21%2F24-orange?style=flat-square&logo=openjdk)

</div>

---

## 目录

- [1. 平台概述](#1-平台概述)
- [2. 核心能力](#2-核心能力)
- [3. 技术架构](#3-技术架构)
  - [3.3 前端界面与交互体系](#33-前端界面与交互体系)
- [4. 权限模型](#4-权限模型)
- [5. 认证与安全机制](#5-认证与安全机制)
- [6. 权限缓存与一致性](#6-权限缓存与一致性)
- [7. 数据权限与 SQL 拦截](#7-数据权限与-sql-拦截)
- [8. 审计与链路追踪](#8-审计与链路追踪)
- [9. 集成测试与质量保障](#9-集成测试与质量保障)
- [10. 性能验证](#10-性能验证)
- [11. 数据库设计](#11-数据库设计)
- [12. 接口概览](#12-接口概览)
- [13. 项目结构](#13-项目结构)
- [14. 环境要求](#14-环境要求)
- [15. 快速开始](#15-快速开始)
- [16. 构建与部署](#16-构建与部署)
- [17. 运行边界](#17-运行边界)

---

## 1. 平台概述

本平台采用前后端分离架构，以 RBAC 权限模型为核心，统一管理用户、角色、菜单、部门及业务数据访问范围。系统不仅控制页面与接口是否可访问，还通过 MyBatis 拦截器在 SQL 执行前注入数据范围条件，实现行级数据隔离。

平台具备以下技术特征：

- **四层权限贯通**：菜单路由、前端按钮、后端接口、数据行分别由独立机制控制。
- **细粒度数据范围**：支持全部数据、本部门数据、本部门及子部门数据三种模式。
- **权限即时生效**：角色授权、权限回收、用户换部门后，无需重新登录即可在后续请求生效。
- **缓存韧性设计**：围绕缓存穿透、击穿、雪崩与一致性提供处理策略，并在 Redis 异常时降级查询数据库。
- **完整审计链路**：操作日志与权限变更日志支持 `trace_id` 串联，权限变更保留新旧菜单差异。
- **可回归验证**：提供连接真实 MySQL、Redis 与完整安全过滤链的集成测试。

---

## 2. 核心能力

| 模块 | 主要能力 |
|---|---|
| 用户管理 | 用户增删改查、分页查询、部门归属、角色分配、账号启停、密码修改 |
| 角色管理 | 角色增删改查、菜单授权、数据范围配置、权限变更审计 |
| 菜单管理 | 目录、菜单、按钮三级节点管理、动态路由、权限标识维护 |
| 部门管理 | 多级部门树、祖级路径维护、子部门删除保护 |
| 认证授权 | JWT 签发与校验、Token 黑名单、登录失败锁定、BCrypt 密码存储 |
| 数据权限 | 全部数据、本部门、本部门及子部门，支持分页 Count 同步过滤 |
| 权限缓存 | 用户权限快照缓存、缓存重建互斥锁、空值缓存、随机 TTL、变更失效 |
| 审计日志 | 操作审计、权限变更 Diff、请求参数脱敏、链路追踪 |
| 业务管理 | 资产、订单、工单三类业务数据，共用统一数据权限机制 |
| 前端界面 | Design Token、紧凑筛选、可拖拽表格、详情抽屉、响应式认证页 |

### 2.1 当前开发运行库规模

当前开发运行库快照如下：

| 数据对象 | 规模 |
|---|---:|
| 用户 | 120 |
| 角色 | 3 |
| 菜单节点 | 34 |
| 部门节点 | 18 |
| 资产记录 | 200 |
| 订单记录 | 160 |
| 工单记录 | 140 |
| 操作日志 | 600 |
| 权限变更日志 | 20 |

> 以上为当前开发运行库快照，不属于代码中的硬编码前提。数据权限测试通过实时查询计算期望结果，不依赖固定行数。
>
> 初始化脚本提供的是可重复构建的基础数据集，其中业务表基线为 20 条资产、15 条订单、12 条工单，部门树为 6 个节点；运行库经过后续数据扩展与精修后，规模可能与脚本基线不同。

### 2.2 代码规模

| 项目 | 数量 |
|---|---:|
| 后端 Java 源文件 | 96 |
| MyBatis Mapper XML | 11 |
| 前端 Vue / JavaScript 源文件 | 34 |
| 前端 API 封装 | 11 |
| 控制器 HTTP 映射 | 52 |
| 数据库表 | 11 |
| 集成测试用例 | 7 |

> 统计口径：后端 Java 源文件为 `springboot/src/main/java` 下 `.java` 文件数（不含测试代码）；Mapper XML 为 `src/main/resources/mapper` 下 `.xml` 文件数；前端源文件为 `vue/src` 下 `.vue` 与 `.js` 文件数；前端 API 封装为 `vue/API` 下文件数；控制器 HTTP 映射为 Controller 中方法级请求映射注解（`@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping`）的出现次数，不含类级 `@RequestMapping` 路径前缀；集成测试用例为 `@Test` 方法数。

---

## 3. 技术架构

```text
浏览器 / 客户端
      │
      │ HTTPS / JSON
      ▼
Vue 3 管理界面
  ├─ 动态路由
  ├─ 按钮权限指令
  ├─ Axios 请求封装
  └─ Pinia 状态管理
      │
      ▼
Spring Boot 3 后端
  ├─ Spring Security 过滤链
  ├─ JWT 认证过滤器
  ├─ Controller
  ├─ AOP 权限校验 / 操作审计
  ├─ Service 事务边界
  └─ MyBatis
      │
      ├─ PageHelper 分页
      ├─ DataScopeInterceptor
      └─ Mapper XML
      │
      ├─ MySQL 8.0
      └─ Redis / Redisson
```

### 3.1 后端技术栈

| 技术 | 版本 | 用途 |
|---|---|---|
| Spring Boot | 3.5.9 | 应用框架与自动配置 |
| Spring Security | 6.x | 认证过滤链与安全上下文 |
| MyBatis Spring Boot Starter | 3.0.5 | 持久层框架（MyBatis 核心版本由 starter 传递） |
| PageHelper | 2.1.0 | 物理分页 |
| JSqlParser | 4.7 | SQL AST 解析与数据权限条件注入 |
| JJWT | 0.11.5 | JWT 签发与解析 |
| Spring Data Redis | 3.5.x | Redis 操作与缓存序列化 |
| Redisson | 3.45.0 | 分布式互斥锁 |
| Druid | 1.2.23 | 数据库连接池 |
| Knife4j / OpenAPI 3 | 4.4.0 / 2.7.0 | 接口文档 |
| JUnit 5 / Spring Boot Test | 5.12 / 3.5.9 | 集成测试 |

### 3.2 前端技术栈

| 技术 | 版本 | 用途 |
|---|---|---|
| Vue | 3.5.40 | 响应式界面框架 |
| Vite | 8.1.5 | 开发与构建工具 |
| Vue Router | 5.2.0 | 路由与动态菜单 |
| Pinia | 4.0.2 | 全局状态管理 |
| Element Plus | 2.14.4 | UI 组件库 |
| vxe-pc-ui | 4.16.28 | 弹窗与表单容器 |
| vxe-table | 4.20.12 | 表格组件 |
| Axios | 1.19.0 | HTTP 客户端 |

### 3.3 前端界面与交互体系

前端界面采用统一的企业后台视觉规范，在保持业务逻辑不变的前提下提升信息密度和可读性：

- `vue/src/assets/styles/all.css` 定义颜色、字体、边框、圆角和阴影等 Design Token；
- `vue/src/assets/styles/pages.css` 提供管理页面的标题、筛选区、表格面板、分页、空状态和表单弹窗样式；
- Element Plus 与 vxe-pc-ui 分工协作，前者承载表格、表单、日期与标签，后者承载新增/编辑弹窗；
- 列表页采用紧凑筛选条和可拖拽列宽的边框表格，长表格支持横向滚动与固定操作列；
- 登录、注册、后台首页、前台用户页和个人中心使用同一套品牌色与响应式布局。

---

## 4. 权限模型

平台采用标准 RBAC 模型，并通过部门维度扩展行级数据权限：

```text
用户 ──< 用户角色 >── 角色 ──< 角色菜单 >── 菜单
  │                    │
  │                    └── data_scope
  │
  └── dept_id ──────────────> 部门 / 子部门
```

| 权限层级 | 控制对象 | 实现方式 | 拒绝结果 |
|---|---|---|---|
| 菜单级 | 页面与路由 | `getRouters` 按角色菜单生成动态路由 | 路由不注册 |
| 按钮级 | 页面操作 | `v-has-perm` 指令 | DOM 节点不渲染 |
| 接口级 | HTTP 写操作 | `@PreAuthorizePerm` + AOP | `body.code=403` |
| 数据级 | 查询结果集 | `@DataScope` + MyBatis 拦截器 | 行数与 `total` 同步收窄 |

菜单权限与数据权限相互独立：用户可以看到某个页面，不代表可以查看该页面中的全部数据。

### 4.1 菜单与动态路由

菜单使用三种类型：

- `M`：目录节点；
- `C`：页面节点；
- `F`：按钮权限节点。

按钮权限保存于 `menu.perms`，例如 `system:user:add`。动态路由由后端根据当前用户授权菜单构建，前端通过 `routeStore` 动态注册。

### 4.2 按钮权限

```vue
<el-button
  v-has-perm="['business:asset:add']"
  type="primary"
  @click="handleAdd"
>
  新增资产
</el-button>
```

`v-has-perm` 从用户状态中读取权限码集合。无权限时节点不会进入 DOM，但前端隐藏仅用于交互控制，真正的安全边界仍在后端接口。

### 4.3 接口权限

```java
@PreAuthorizePerm("system:user:add")
@PostMapping("insertUser")
public AjaxResult insertUser(@RequestBody User user) {
    return toAjax(userService.insertUser(user));
}
```

`PreAuthorizeAspect` 在 Controller 方法执行前校验权限：

- `userId == 1` 的系统管理员直接放行；
- 其他用户必须包含所需权限码；
- 未认证或权限不足时抛出 `AccessDeniedException`；
- 全局异常处理器统一转换为业务响应。

查询接口主要依赖菜单授权与数据范围控制，管理类写接口均配置独立的权限码校验；个人中心的资料与密码修改仅要求登录，不额外设置权限码。

### 4.4 数据范围

| `data_scope` | 范围 | SQL 行为 |
|---:|---|---|
| 1 | 全部数据 | 不追加部门条件 |
| 2 | 本部门 | `dept_id = 当前用户部门` |
| 3 | 本部门及子部门 | `dept_id IN (本部门 + 子部门集合)` |

当用户没有角色或角色的 `data_scope` 为空时，登录与缓存重建均按 `1` 处理；旧 Token 中 `dataScope` 为空时，数据权限拦截器按 `2` 处理，避免旧会话扩大可见范围。

> 两处默认值取向不同是有意为之：登录赋值发生在可信的身份构建阶段，取 `1` 表示“没有配置即不额外限制”；拦截器兜底作用在旧 Token 的会话上，该字段可信度未知，取 `2` 表示“按最小可见范围处理”。

---

## 5. 认证与安全机制

### 5.1 JWT 与无状态会话

系统采用无状态认证，默认不创建 HTTP Session。JWT 包含：

- `jti`：令牌唯一标识；
- `user_key`：用户 ID、部门、数据范围、管理员标识及权限快照；
- `exp`：过期时间。

默认有效期为 300 分钟。请求头支持裸 Token 与 `Bearer <token>` 两种格式。

Token 在有效期内不会被重新签发。用户换部门、角色授权变更等场景依靠第 6.4 节的请求级快照刷新生效，不要求重新登录——这也是“改部门后旧 Token 数据权限不刷新”缺陷的修复思路。

### 5.2 密码安全

- 密码使用 `BCryptPasswordEncoder` 存储；
- 新增、编辑、注册、密码重置均执行加密；
- 登录使用 `passwordEncoder.matches` 校验；
- 不存在明文比较路径：库内密码已全部为 BCrypt（非 BCrypt 用户数 = 0），旧版“明文迁移兼容”分支已移除。

### 5.3 登录失败锁定

同一账号连续输错 5 次密码后锁定 15 分钟：

- Key：`login:fail:{规范用户名}`；
- 首次失败通过 `SET NX EX` 原子创建计数；
- 后续失败递增，不延长固定窗口；
- 登录成功后清除计数；
- Redis 异常时记录告警并降级，不阻断全部登录请求。

### 5.4 Token 黑名单

登出时将当前 Token 的 `jti` 写入 Redis：

```text
token:blacklist:{jti}
```

黑名单 TTL 与 Token 剩余有效期一致。认证过滤器在每次请求中检查黑名单，实现单 Token 失效，不影响同一账号的其他设备会话。

### 5.5 账号状态与异常处理

- 密码校验通过后再检查账号状态，降低账号状态探测风险；
- 参数校验、权限不足、业务异常、请求方式错误、JSON 解析错误统一处理；
- 未认证请求返回 HTTP 401；
- 业务权限不足返回 HTTP 200，响应体 `code=403`；
- 全局兜底不向前端返回堆栈信息。

---

## 6. 权限缓存与一致性

权限缓存是平台的核心组件，用于降低重复鉴权成本并保证权限变更及时生效。

### 6.1 缓存结构

| 项目 | 设计 |
|---|---|
| Key | `perm:user:{userId}` |
| 内容 | 权限码 `perms`、路由树 `routers`、部门 `deptId`、数据范围 `dataScope`、空值标记 |
| 正常 TTL | 18000 秒 ± 300 秒随机偏移 |
| 空结果 TTL | 60 秒 |
| 序列化 | JSON |
| 分布式锁 | `lock:perm:user:{userId}` |

### 6.2 缓存读取与重建

```text
读取 Redis
  ├─ 命中：直接返回
  └─ 未命中：
       ├─ 获取 Redisson 互斥锁
       ├─ 双重检查缓存
       ├─ 查询数据库权限、路由、部门与数据范围
       ├─ 写入缓存
       └─ 释放锁
```

缓存不可用时降级查询数据库；未获得锁时也直接查库，避免请求阻塞。

### 6.3 变更失效

| 变更场景 | 失效方式 |
|---|---|
| 用户部门、角色或账号信息变更 | 按用户删除 `perm:user:{userId}` |
| 角色授权或数据范围变更 | 事务提交后按角色删除全部关联用户缓存 |
| 菜单变更 | 使用 `SCAN` 全量清理权限缓存 |
| 登录 | 删除当前用户缓存，保证后续请求重建 |

角色变更通过 `PermissionChangeAspect` 注册 `afterCommit` 回调，避免事务未提交时提前删除缓存。

### 6.4 Token 快照刷新

JWT 中的 `deptId` 与 `dataScope` 是签发时快照。每个认证请求会从缓存读取最新值并覆盖当前会话：

```java
UserPermCacheVO snapshot = permCacheService.getOrLoad(userId);
loginUser.setPermissions(new HashSet<>(snapshot.getPerms()));

if (snapshot.getDeptId() != null) {
    loginUser.setDeptId(snapshot.getDeptId());
}
if (snapshot.getDataScope() != null) {
    loginUser.setDataScope(snapshot.getDataScope());
}
```

因此：

- 角色权限授予或回收后，旧 Token 立即使用新权限；
- 用户换部门后，旧 Token 在后续请求中使用新部门；
- 角色数据范围变化后，旧 Token 使用新范围；
- 旧缓存缺少新增字段时保留 Token 原值，缓存自然过期后自动升级。

### 6.5 请求级快照复用

Filter 将完整缓存快照写入当前请求属性，`getInfo` 与 `getRouters` 直接复用，避免同一请求重复查询和重复反序列化。

---

## 7. 数据权限与 SQL 拦截

数据权限通过 MyBatis `StatementHandler.prepare` 拦截器实现，不侵入业务 SQL。

```java
@DataScope(deptAlias = "u")
List<User> selectUserList(User user);
```

处理流程：

1. 根据 `MappedStatement` 获取 Mapper 方法；
2. 剥离 PageHelper 的 `_COUNT` 后缀；
3. 读取 Mapper 方法上的 `@DataScope`；
4. 获取当前认证用户的 `deptId` 与 `dataScope`；
5. 使用 JSqlParser 解析原 SQL；
6. 以 AST 节点方式追加部门条件；
7. 执行改写后的 SQL。

PageHelper 的列表查询与 Count 查询使用同一过滤逻辑，保证 `rows` 与 `total` 一致。

### 7.1 部门路径

部门使用 `ancestors` 保存祖级路径，例如：

```text
0,1,2
```

“本部门及以下”通过以下条件获取部门集合：

```sql
SELECT dept_id
FROM sys_dept
WHERE dept_id = ?
   OR FIND_IN_SET(?, ancestors)
```

该方式避免应用层递归查询整棵部门树。

### 7.2 异常策略

当前 SQL 解析异常时记录错误日志并继续执行原 SQL，优先保障业务可用性。该行为属于可用性优先的降级策略；对安全敏感场景，建议后续切换为 fail-closed，避免解析失败导致范围条件未注入。

---

## 8. 审计与链路追踪

### 8.1 操作审计

业务三表（资产、订单、工单）的增删改，以及用户新增与用户状态变更，通过 `@OperationLog` 注解记录（当前共 11 处）：

- 操作人；
- 模块与业务动作；
- 请求 URI 与方法；
- 请求参数；
- 执行结果；
- 耗时；
- 执行状态；
- `trace_id`；
- 操作时间。

密码相关字段在写入日志前统一脱敏，日志异步持久化，不阻塞主业务流程。

> 说明：`updateUser`、删除用户，以及角色、菜单、部门的写接口暂未配置该注解。这些变更可通过 8.2 节的权限变更日志，或数据库记录的 `update_time` 追溯。

### 8.2 权限变更审计

角色授权变化记录：

- 变更前菜单集合；
- 变更后菜单集合；
- 新增菜单；
- 移除菜单；
- 操作人；
- 目标角色；
- `trace_id`；
- 操作时间。

### 8.3 链路追踪

`TraceIdFilter` 为每个请求生成或透传链路标识，并写入 MDC。操作日志、权限变更日志及应用日志可通过同一 `trace_id` 串联。

---

## 9. 集成测试与质量保障

项目提供两类集成测试，均启动完整 Spring 上下文与 MockMvc 安全过滤链，连接真实 MySQL 和 Redis。

### 9.1 `AuthAndPermissionTest`

覆盖 6 个核心场景：

1. 管理员正确登录并获取权限；
2. 错误密码返回业务错误并记录失败次数；
3. 普通用户调用写接口被拒绝且无可见写入；
4. 管理员新增用户、建立角色关联并验证 BCrypt 与部门落库；
5. 普通用户与管理员的数据范围对照；
6. 同一旧 Token 在权限授予与回收后分别从 403 变为 200、再回到 403。

### 9.2 `DeptScopeRefreshTest`

覆盖部门变更后的会话一致性：

1. 夹具用户在部门 4 登录；
2. 验证旧 Token 只能看到部门 4 数据；
3. 管理员将用户调整到部门 5；
4. 确认数据库 `dept_id=5`；
5. 继续使用原 Token；
6. 验证后续查询只返回部门 5 数据。

该用例已完成反向验证：移除 Filter 中的快照覆盖逻辑后，测试按预期失败，证明用例能够检测原始缺陷。

### 9.3 测试隔离与清理

- 使用独立夹具角色 `ittest_role`；
- 使用独立夹具用户 `ittest_user`；
- 临时用户统一使用 `ittest_case` 前缀；
- 每个用例前后重置角色菜单与缓存；
- 类级结束后删除夹具、临时用户及测试审计日志；
- 对清理结果执行自动断言；
- 不修改真实角色授权；
- 不清理真实用户的 Redis 状态。

测试要求串行执行，不适用于多个进程同时连接同一开发库的场景。

### 9.4 最近验证结果

```text
AuthAndPermissionTest    Tests run: 6, Failures: 0, Errors: 0
DeptScopeRefreshTest     Tests run: 1, Failures: 0, Errors: 0
合计                     Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

执行：

```bash
cd springboot
mvn clean test
```

> 运行前需确认 `springboot/src/test/resources/application-test.yml` 中的 MySQL 与 Redis 连接可达——该文件覆盖了数据源账号、JWT 密钥与日志级别。若 `mvn` 未加入 PATH，请使用 Maven 安装目录下的完整路径执行。

---

## 10. 性能验证

性能验证基于单机 JMeter 压测，测试对象为 `/getInfo` 与 `/system/menu/getRouters`，压测数据集为 5 个用户、8 个菜单、2 个角色，小于当前开发运行库规模。

| 阶段 | 每请求 SQL | 吞吐 | P95 |
|---|---:|---:|---:|
| 原始实现 | 6.50 | 92.0 次/s | 1138 ms |
| 消除请求内重复查询 | 3.51 | 165.7 次/s | 645 ms |
| 叠加 Redis 权限缓存 | 0.50 | 1051.0 次/s | 104 ms |

端到端吞吐约为初始版本的 11.4 倍。该结果来自特定单机环境、特定数据规模（5 用户 / 8 菜单 / 2 角色）与单轮采样，用于说明优化方向，不作为生产容量承诺。完整实验设计、原始数据与限制见 [性能验证报告](docs/performance.md)。

---

## 11. 数据库设计

平台共包含 11 张表：

| 分类 | 表 | 用途 |
|---|---|---|
| 权限核心 | `user` | 用户、部门、状态、密码 |
| 权限核心 | `role` | 角色与数据范围 |
| 权限核心 | `menu` | 菜单、按钮与权限标识 |
| 关联关系 | `user_role` | 用户与角色多对多关系 |
| 关联关系 | `role_menu` | 角色与菜单多对多关系 |
| 组织 | `sys_dept` | 多级部门与祖级路径 |
| 审计 | `sys_operation_log` | 操作审计 |
| 审计 | `sys_permission_log` | 权限变更审计 |
| 业务 | `asset` | 资产台账 |
| 业务 | `biz_order` | 订单台账 |
| 业务 | `biz_ticket` | 工单台账 |

### 11.1 关键约束

- `user.user_name` 唯一；
- `user_role` 使用 `(user_id, role_id)` 复合主键；
- `role_menu` 使用 `(role_id, menu_id)` 复合主键；
- 业务表均包含 `dept_id` 与对应索引；
- 用户新增与编辑均支持部门写入；
- 部门删除前检查是否存在子部门。

### 11.2 数据库初始化

仓库提供两套基础脚本：

```bash
mysql -u root -p fast < docs/fast.sql
mysql -u root -p fast < docs/init-business-data.sql
```

> `docs/fast.sql` 会重建基础表，`docs/init-business-data.sql` 会重建业务表并重置部分角色、用户与部门数据。执行前必须确认目标数据库，避免影响已有数据。

---

## 12. 接口概览

统一响应结构：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {},
  "timestamp": 1763961600000
}
```

常见业务状态：

| Code | 含义 |
|---:|---|
| 200 | 成功 |
| 400 | 参数或请求格式错误 |
| 401 | 未认证或登录失效 |
| 403 | 权限不足 |
| 500 | 业务或系统错误 |

### 12.1 认证与用户上下文

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/login` | 登录并签发 JWT |
| POST | `/logout` | 当前 Token 加入黑名单 |
| POST | `/register` | 用户注册 |
| GET | `/getInfo` | 当前用户信息与权限码 |
| GET | `/system/menu/getRouters` | 当前用户动态路由 |
| POST | `/system/user/profile/avatar` | 上传头像 |
| PUT | `/system/user/profile` | 修改个人资料 |
| PUT | `/system/user/profile/updatePwd` | 修改密码 |

### 12.2 用户管理

| 方法 | 路径 | 权限 |
|---|---|---|
| GET | `/system/user/selectUserList` | 登录 + 数据范围 |
| GET | `/system/user/selectUserById/{userId}` | 登录 |
| POST | `/system/user/insertUser` | `system:user:add` |
| PUT | `/system/user/updateUser` | `system:user:edit` |
| PUT | `/system/user/changeStatus` | `system:user:edit` |
| DELETE | `/system/user/deleteUserByUserIds/{userIds}` | `system:user:remove` |

### 12.3 角色、菜单与部门

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/system/role/selectAllRole` | 角色列表 |
| GET | `/system/role/selectRoleList` | 角色分页 |
| GET | `/system/role/selectRoleById/{roleId}` | 角色详情 |
| POST | `/system/role/insertRole` | 新增角色 |
| POST | `/system/role/updateRole` | 修改角色与授权 |
| DELETE | `/system/role/deleteRoleByRoleIds/{roleIds}` | 删除角色 |
| GET | `/system/menu/selectMenuList` | 菜单列表 |
| GET | `/system/menu/selectMenuByMenuId/{menuId}` | 菜单详情 |
| GET | `/system/menu/selectRoleMenuTree/{roleId}` | 角色授权树（带勾选状态） |
| GET | `/system/menu/selectRoleMenusTree` | 全量菜单下拉树（不含勾选状态） |
| POST | `/system/menu/insertMenu` | 新增菜单 |
| PUT | `/system/menu/updateMenu` | 修改菜单 |
| DELETE | `/system/menu/deleteMenuByMenuId/{menuId}` | 删除菜单 |
| GET | `/system/dept/selectDeptList` | 部门树 |
| GET | `/system/dept/selectDeptById/{deptId}` | 部门详情 |
| GET | `/system/dept/selectDeptTree` | 部门下拉树 |
| POST | `/system/dept/insertDept` | 新增部门 |
| PUT | `/system/dept/updateDept` | 修改部门 |
| DELETE | `/system/dept/deleteDeptById/{deptId}` | 删除部门 |

### 12.4 审计日志

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/monitor/operlog/selectList` | 操作日志分页 |
| GET | `/monitor/permlog/selectList` | 权限变更日志分页 |

### 12.5 业务接口

业务模块统一采用以下命名：

| 操作 | 路径 |
|---|---|
| 列表 | `/business/{module}/select{Module}List` |
| 详情 | `/business/{module}/select{Module}ById/{id}` |
| 新增 | `/business/{module}/insert{Module}` |
| 修改 | `/business/{module}/update{Module}` |
| 删除 | `/business/{module}/delete{Module}ById/{id}` |

其中 `{module}` 为 `asset`、`order`、`ticket`。写接口分别校验 `business:{module}:add/edit/remove`。

完整 OpenAPI 文档默认位于：

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

当前 `knife4j.enable=false`，Knife4j 增强页面 `/doc.html` 不默认启用；如需使用，可在配置中显式开启。

---

## 13. 项目结构

```text
RBAC/
├── springboot/
│   ├── src/main/java/com/fast/
│   │   ├── system/
│   │   │   ├── annotation/       # 权限、数据范围、操作日志注解
│   │   │   ├── aspect/           # 权限校验、审计、缓存失效切面
│   │   │   ├── config/           # Security、MyBatis、资源与文档配置
│   │   │   ├── configure/        # JWT、Token 黑名单与认证过滤器
│   │   │   ├── controller/       # HTTP 接口
│   │   │   ├── domain/           # 实体、响应对象与缓存 VO
│   │   │   ├── enums/            # 枚举
│   │   │   ├── exception/        # 业务异常与全局处理
│   │   │   ├── filter/           # TraceId 过滤器
│   │   │   ├── interceptor/      # 数据权限 SQL 拦截器
│   │   │   ├── mapper/           # MyBatis Mapper
│   │   │   ├── service/          # 业务服务
│   │   │   └── utils/            # 安全上下文等工具
│   │   ├── src/main/resources/
│   │   │   ├── application.yml
│   │   │   └── mapper/system/
│   │   └── src/test/
│   │       ├── java/com/fast/system/it/
│   │       └── resources/application-test.yml
│   └── pom.xml
│
├── vue/
│   ├── API/                      # 系统、日志与业务接口封装
│   ├── src/
│   │   ├── assets/
│   │   │   └── styles/
│   │   │       ├── all.css             # Design Token 与全局基础样式
│   │   │       └── pages.css           # 管理页面共享布局与组件样式
│   │   ├── components/
│   │   ├── directive/            # v-has-perm
│   │   ├── router/
│   │   ├── stores/
│   │   ├── utils/
│   │   ├── views/
│   │   │   ├── layout/
│   │   │   ├── system/
│   │   │   ├── monitor/
│   │   │   ├── business/
│   │   │   └── userPage/              # 前台用户首页与容器
│   │   └── permission.js         # 动态路由守卫
│   ├── vite/
│   ├── .env.development
│   ├── .env.production
│   ├── vite.config.js
│   └── package.json
│
├── docs/                         # 初始化 SQL、性能数据与技术文档
└── README.md
```

---

## 14. 环境要求

| 组件 | 要求 |
|---|---|
| JDK | 21 或 24 |
| Maven | 3.9+ |
| Node.js | 支持 Vite 8 的 Node.js 20.19+ 或 22.12+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |
| 操作系统 | Windows / Linux / macOS |

---

## 15. 快速开始

### 15.1 初始化数据库

创建数据库：

```sql
CREATE DATABASE fast
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_bin;
```

执行初始化脚本：

```bash
mysql -u root -p fast < docs/fast.sql
mysql -u root -p fast < docs/init-business-data.sql
```

> 两个脚本包含 `DROP TABLE` 与数据重置操作，仅可在空库或已备份环境执行。

### 15.2 配置 Redis

修改 `springboot/src/main/resources/application.yml`：

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: your-redis-password
      database: 1
      timeout: 3s
```

### 15.3 配置环境变量

数据库账号与 JWT 密钥通过环境变量注入。

Windows PowerShell：

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-database-password"
$env:JWT_SECRET = "replace-with-a-random-secret-of-at-least-32-characters"
```

Linux / macOS：

```bash
export DB_USERNAME=root
export DB_PASSWORD="your-database-password"
export JWT_SECRET="replace-with-a-random-secret-of-at-least-32-characters"
```

### 15.4 启动后端

```bash
cd springboot
mvn clean spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

接口文档：

```text
http://localhost:8080/swagger-ui.html
```

### 15.5 启动前端

```bash
cd vue
npm install
npm run dev
```

默认地址：

```text
http://localhost:90
```

开发代理配置位于 `vue/vite.config.js`：

- `/base` 转发到 `http://localhost:8080` 并移除前缀；
- `/profile` 转发到后端静态资源路径。

### 15.6 初始化脚本默认账号

| 账号 | 密码 | 部门 | 角色 | 数据范围 |
|---|---|---|---|---|
| `admin` | `admin123` | 总经办（dept 1） | 系统管理员 | 全部数据 |
| `研发部经理` | `123456` | 研发部（dept 2） | 部门管理员 | 本部门及子部门 |
| `张三` | `123456` | 前端组（dept 4） | 普通用户 | 本部门 |

> 账号、部门与密码来自初始化脚本，三条已逐项核对（含 BCrypt 密码校验）。

> 默认账号仅用于本地验证。部署到共享环境前必须修改密码，并移除不需要的公开注册入口。

---

## 16. 构建与部署

项目采用前后端独立构建，当前仓库不包含 Dockerfile、Docker Compose 或 CI 流水线。发布时需要分别构建后端 JAR 与前端静态资源，并由部署环境负责进程管理、反向代理和日志收集。

### 16.1 后端打包

```bash
cd springboot
mvn clean package -DskipTests
java -jar target/springboot-0.0.1-SNAPSHOT.jar
```

运行前设置 `DB_USERNAME`、`DB_PASSWORD` 与 `JWT_SECRET`。

### 16.2 前端构建

```bash
cd vue
npm run build
```

构建产物默认输出至 `vue/dist/`。

生产环境修改 `vue/.env.production`，将示例地址替换为实际 API 前缀：

```dotenv
VITE_APP_BASE_API = '/api'
```

Nginx 反向代理示例：

```nginx
server {
    listen 80;
    server_name your-domain;

    root /path/to/vue/dist;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /profile/ {
        proxy_pass http://127.0.0.1:8080;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 16.3 部署检查清单

- 修改默认账号密码；
- 使用高强度随机 JWT 密钥；
- 限制数据库与 Redis 网络访问；
- 按环境修改跨域允许来源；
- 关闭或限制公开注册入口；
- 为前端配置生产 API 地址；
- 备份数据库并建立恢复流程；
- 根据访问量调整连接池与 Tomcat 线程参数。

---

## 17. 运行边界

1. **应用范围**：当前实现面向单应用 RBAC 场景，不包含跨应用授权、多租户与组织隔离。
2. **数据范围**：当前支持全部、本部门、本部门及子部门，不支持仅本人或自定义数据集合。
3. **负责人维度**：业务表的 `owner_user_id` 暂不参与数据过滤，唯一权限锚点为 `dept_id`。
4. **部门调整**：当前部门修改不更新 `parent_id` 与 `ancestors`，不支持通过编辑接口迁移部门父级。
5. **SQL 降级**：数据权限 SQL 解析失败时当前为放行策略，安全敏感部署应调整为拒绝执行。
6. **测试环境**：集成测试连接真实 MySQL 与 Redis，要求串行执行，不包含 Testcontainers 与并发隔离。
7. **认证边界**：JWT 默认有效期 300 分钟，未提供刷新 Token 与多因素认证。
8. **公开入口**：系统包含注册入口，生产部署应按业务需求关闭或增加审批机制。
9. **性能数据**：现有压测为单机、小数据集与单轮采样结果，不代表分布式生产环境容量。
10. **缓存兼容**：旧缓存缺少 `deptId/dataScope` 时保留 Token 原值，待缓存 TTL 到期后自动升级；需要立即升级时可执行权限缓存全量失效。
11. **开发代理边界**：开发环境直接访问 `/profile/user` 会被 `/profile` 静态资源代理转发到后端，应通过站内入口进入个人中心；生产环境需确保 Nginx 的 API 代理与 SPA 回退规则顺序正确。
12. **构建边界**：仓库不包含容器编排与 CI 配置，构建结果需要由外部部署环境发布和运行。
