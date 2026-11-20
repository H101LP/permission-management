# RBAC 权限管理系统

**Spring Boot 3 + Vue 3 前后端分离权限系统 —— 菜单级、接口级、按钮级、数据级四层权限，一套模型贯通。**

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen?style=flat-square&logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.5.40-4FC08D?style=flat-square&logo=vuedotjs)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-Redisson%203.45-DC382D?style=flat-square&logo=redis)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?style=flat-square&logo=springsecurity)
![JDK](https://img.shields.io/badge/JDK-21-orange?style=flat-square)

</div>

---

## 项目定位

一个**可运行、可验证**的 RBAC 权限系统实现。与常见的"权限 CRUD 脚手架"的区别在于：权限不只是"能不能点"，而是沿四个层级真正落地，并且每一层都有可复现的验收数据。

| 层级 | 控制内容 | 实现载体 |
|---|---|---|
| 菜单级 | 看得到 / 看不到 | `getRouters` 按 `role_menu` 裁剪菜单树 |
| 接口级 | 调得通 / 403 | `@PreAuthorizePerm` + `PreAuthorizeAspect` |
| 按钮级 | 显不显示 | `v-has-perm` 指令 |
| **数据级** | **看得到多少行** | **`@DataScope` + JSqlParser 拦截器** |

前三层是常规做法，**第四层（行级数据权限）是这套系统的重点**。它把 `role.data_scope` 与部门树结合，在不改业务 SQL 的前提下，于 MyBatis 执行前重写 `WHERE` 条件。

---

## 核心能力

| 模块 | 能力 |
|---|---|
| 用户管理 | 增删改查、分页、角色分配、状态启停、部门归属 |
| 角色管理 | 增删改查、菜单授权、**数据范围配置（3 种）**、权限变更审计 |
| 菜单管理 | 树形菜单（目录 / 菜单 / 按钮三型）、动态路由数据源 |
| 部门管理 | 多级部门树（`parent_id` + `ancestors` 路径枚举） |
| 数据权限 | 全部 / 本部门 / 本部门及以下，注解式接入，业务代码零侵入 |
| 登录认证 | JWT 签发与校验、登出即失效、失败锁定 |
| 权限缓存 | Redis 缓存菜单树与权限码，变更后事务提交时失效 |
| 审计日志 | 操作日志 + 权限变更日志，均可查询分页 |
| 业务台账 | 资产 / 订单 / 工单三张表，共用同一套数据权限 |

---

## 数据权限：三种范围

### 范围定义

| `data_scope` | 含义 | 生成的 SQL 条件 |
|---|---|---|
| 1 | 全部数据 | 不追加条件（`isAdmin()` 或 scope=1 直接放行） |
| 2 | 本部门 | `AND dept_id = {当前用户部门}` |
| 3 | 本部门及以下 | `AND dept_id IN (本部门 + 所有子部门)` |

子部门集合由 `sys_dept.ancestors` 路径字段推导，避免递归查树。`scope` 为空时**按 2 处理**（fail-close，宁可少看不可多看）。

### 接入方式

业务侧只在 Mapper 接口方法上贴一行注解，别名与 XML 主表别名一致即可：

```java
@DataScope(deptAlias = "u")
List<User> selectUserList(User user);
```

```xml
<select id="selectUserList" resultMap="UserResult">
  SELECT u.user_id, u.user_name, ... FROM user u
  <where>
    <if test="userName != null and userName != ''">
      AND u.user_name LIKE CONCAT('%', #{userName}, '%')
    </if>
  </where>
</select>
```

### 拦截器做了什么

`DataScopeInterceptor` 实现 MyBatis `Interceptor`，在 SQL 构建阶段介入：

1. 从 `LoginUser` 取 `userId` 与 `dataScope`
2. 剥离 PageHelper 生成的 `_COUNT` 后缀，兼容分页计数
3. 用 JSqlParser 解析原 SQL，定位 `FROM` 后的主表别名
4. 按 `scope` 生成条件表达式，以 `AndExpression` 合并到原 `WHERE`
5. 解析失败或异常时**降级放行**（记录日志，不阻断业务）

不拼接字符串、不做正则替换——条件以 AST 节点注入，注入面收敛。

### 实测结果

三个业务台账表（资产 20 条 / 订单 15 条 / 工单 12 条，分布在 5 个部门）：

| 账号 | 角色 / 范围 | 资产 | 订单 | 工单 |
|---|---|---|---|---|
| admin | 系统管理员 / 全部(1) | 20 | 15 | 12 |
| 研发部经理 | 部门管理员 / 本部门及以下(3) | 16 | 12 | 9 |
| 张三（前端组） | 普通用户 / 本部门(2) | 6 | 5 | 4 |

用户列表接口同口径验证：`admin` 8 条、`研发部经理` 6 条、`张三` 2 条。三张表的可见行数比例接近，说明是**同一套逻辑在生效**，而非某张表的特例。

`EXPLAIN` 验证三张业务表改写后的查询均命中 `idx_dept_id`（`type=ref`），未退化为全表扫描。

---

## 安全机制

| 项 | 设计 |
|---|---|
| 密码存储 | BCrypt（`BCryptPasswordEncoder`），库内全部为密文，无明文兼容分支 |
| Token | JWT，有效期 300 分钟，载荷含 `userId` / `deptId` / `dataScope` / `admin` |
| 登出 | **jti 粒度黑名单**写入 Redis，TTL = 令牌剩余寿命；A 设备登出不影响 B 设备 |
| 失败锁定 | 同一账号连续 5 次密码错误锁定 15 分钟，计数存 Redis |
| 越权防护 | 写接口全部 `@PreAuthorizePerm`，普通用户 403，admin 由 `userId == 1` 直放 |
| 异常处理 | 统一 `GlobalExceptionHandler`：403 / 405 / 业务异常 / 兜底，不泄漏堆栈 |

> 一个踩过的实现细节：在 spring-data-redis 3.4.x 下对 `StringRedisTemplate` 调用 `expire(...)` 会触发 `pExpire` 自递归并抛出 `StackOverflowError`（是 `Error` 不是 `Exception`，`catch(Exception)` 抓不住）。本项目统一改用 `setIfAbsent(key, value, ttl, unit)` 首次创建即带过期时间，**禁止直接调用 `expire`**。

---

## 权限缓存

`/getInfo` 与 `/getRouters` 原先每次都要多表关联查询菜单，改为缓存后只查一次库。

| 项 | 设计 |
|---|---|
| Key | `perm:user:{userId}` |
| 内容 | 菜单树 `routers` + 权限码 `perms`（JSON） |
| TTL | 18000s（与 JWT 有效期一致）+ 随机 ±300s 防同时失效 |
| 读 | 先查缓存，命中即返回；未命中查库回写，Redisson 互斥锁防击穿 |
| 穿透 | 空结果缓存 60s |
| 失效 | 事务提交后删缓存（`afterCommit`），避免"缓存已删、事务回滚"造成脏读 |
| 扩散 | 角色/菜单变更时按 `roleId` 批量清该角色下所有用户的缓存 |

实测：连续两次 `getInfo`，第 1 次触发缓存重建（3 条菜单相关 SQL），第 2 次仅剩 1 条用户基础信息查询。

---

## 技术栈

**后端**

- Spring Boot 3.5.9 · Spring Security 6.x · JDK 21
- MyBatis 3.0.5 · PageHelper 2.1.0 · Druid 1.2.23
- **JSqlParser 4.7**（数据权限 SQL 改写）
- JJWT 0.11.5 · Lombok
- Spring Data Redis + **Redisson 3.45.0**
- knife4j 4.4.0（OpenAPI 3 接口文档）
- MySQL 8.0 · Maven 3.9

**前端**

- Vue 3.5.40 · Vite · Pinia · Vue Router 5
- Element Plus · VxeUI · Axios

---

## 数据库设计

共 **11 张表**：

| 分类 | 表 | 说明 |
|---|---|---|
| 权限核心 | `user` / `role` / `menu` | 用户、角色、菜单（含 M/C/F 三型） |
| 关联 | `user_role` / `role_menu` | 复合主键，多对多 |
| 组织 | `sys_dept` | 多级部门，`parent_id` + `ancestors` 路径枚举 |
| 日志 | `sys_operation_log` / `sys_permission_log` | 操作审计、权限变更审计 |
| 业务台账 | `asset` / `biz_order` / `biz_ticket` | 三表同构，均带 `dept_id` 作为数据权限锚点 |

业务三表的设计约束：

- **`dept_id` 是唯一权限锚点**，三表都有，所以一套拦截逻辑能同时覆盖三个模块
- **只做台账，不做流程**：无支付、无库存、无审批流、无商品关联表
- `status` 用 `TINYINT` 而非字符串，索引友好，语义由前端字典翻译
- 每张表建 `idx_dept_id`——数据权限会追加 `dept_id` 条件，无索引即全表扫

---

## 项目结构

```text
RBAC/
├── springboot/                          # 后端
│   ├── src/main/java/com/fast/system/
│   │   ├── annotation/                  # @DataScope、@PreAuthorizePerm
│   │   ├── aspect/                      # 权限校验切面、权限变更审计切面
│   │   ├── configure/                   # Security、MyBatis（拦截器注册）、Redis
│   │   ├── controller/                  # 登录、用户、角色、菜单、部门、日志
│   │   ├── domain/                      # 实体与 VO（LoginUser、RouterVo、缓存 VO）
│   │   ├── interceptor/                 # DataScopeInterceptor（JSqlParser）
│   │   ├── mapper/                      # MyBatis Mapper 接口
│   │   ├── service/                     # 业务层（含权限缓存服务）
│   │   └── utils/                       # JWT、SecurityUtils、上下文
│   ├── src/main/resources/
│   │   ├── application.yml              # 数据源 / Redis / JWT（环境变量注入）
│   │   └── mapper/                      # MyBatis XML
│   └── pom.xml
│
├── vue/                                 # 前端
│   ├── src/
│   │   ├── API/  components/  router/  stores/  utils/
│   │   ├── directive/hasPerm.js         # v-has-perm
│   │   ├── views/                       # system / layout / login
│   │   └── permission.js                # 路由守卫（动态路由注入）
│   ├── vite.config.js
│   └── package.json
│
├── docs/                                # 设计文档、SQL、压测数据
└── README.md
```

---

## 快速开始

### 环境要求

| 工具 | 版本 |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |

### 初始化数据库

库名 `fast`，按顺序执行两步：

```bash
# 1. 基础层：8 张表（user / role / menu / user_role / role_menu / sys_dept / 两张日志表）
mysql -u root -p fast < docs/fast.sql

# 2. 业务承载层：3 张业务表 + 部门管理员角色 + 用户部门重分布 + 47 条演示数据
mysql -u root -p fast < docs/init-business-data.sql
```

第二步执行完会返回 `asset 20 / biz_order 15 / biz_ticket 12` 三个计数，与下文的数据权限验证数字一一对应。

> 两个脚本均为幂等设计（`DROP TABLE IF EXISTS` / 先删后插），可重复执行。


### 启动后端

`application.yml` 中数据库与 Redis 连接、JWT 密钥均通过环境变量注入：

```bash
# Windows
set DB_USERNAME=root
set DB_PASSWORD=你的数据库密码
set JWT_SECRET=至少32位的密钥

cd springboot
mvn spring-boot:run
```

```bash
# Linux / macOS
export DB_USERNAME=root
export DB_PASSWORD=你的数据库密码
export JWT_SECRET=至少32位的密钥

cd springboot
mvn spring-boot:run
```

启动前修改 `application.yml` 中的 Redis 地址与密码。后端默认端口 `8080`。

### 启动前端

```bash
cd vue
npm install
npm run dev
```

访问控制台输出的地址（默认 `http://localhost:90`）。

**演示账号**

| 账号 | 密码 | 角色 | 数据范围 |
|---|---|---|---|
| `admin` | `admin123` | 系统管理员 | 全部 |
| `研发部经理` | `123456` | 部门管理员 | 本部门及以下 |
| `张三` | `123456` | 普通用户 | 本部门 |

三个账号的可见行数分别是 20 / 16 / 6（资产表），可直观验证数据权限。

---

## 部署

```bash
# 后端
cd springboot
mvn clean package -DskipTests
java -jar target/*.jar

# 前端
cd vue
npm run build
```

`dist/` 交付 Nginx，`/api` 反向代理到后端 `8080`。

---

## 已知边界

写在这里是为了避免误读，也是设计取舍的说明：

- **不是中台**。严格意义的中台需要多应用接入（`sys_app` + 跨应用鉴权），本项目是**单应用内的完整权限模型**，可以说"具备中台雏形"，不宣称"是中台"。
- **数据权限只认 `dept_id`**，不支持"仅本人""自定义部门"两种范围；`owner_user_id` 字段已预留但未参与过滤。
- **业务三表是权限的验证载体，不是业务系统**：无支付、无库存、无审批流、无状态机。
- **未做**：第三方登录、验证码、多租户、微服务拆分、容器编排。权限变更是低频写，未引入消息队列。
- **压测与性能数据基于单机环境**，不构成生产容量结论。
- 菜单、权限、部门的新增与修改依赖前端配置页，暂未提供开放 API 供外部系统调用。

---

## 文档

| 文档 | 内容 |
|---|---|
| `docs/批次3-档B升级方案.md` | 完整升级方案：现状核查、分阶段步骤、验收标准、工时拆解 |
| `docs/批次2-数据库与安全加固.md` | 数据库改造与安全加固记录 |
| `docs/performance.md` | 接口压测数据与环境说明 |
| `docs/路线-最终版-2026-11-16.md` | 升级路线与取舍依据 |
| `docs/fast.sql` | 基础层建表与初始数据（8 张表） |
| `docs/init-business-data.sql` | 业务承载层初始化（3 张业务表 + 角色 + 47 条演示数据） |

---

<div align="center">

**Spring Boot 3 × Vue 3 × RBAC × Redis × JSqlParser**

</div>
