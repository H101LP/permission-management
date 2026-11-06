# 🚀 RBAC 权限管理系统

**基于 Spring Boot 3 + Vue 3 的前后端分离 RBAC 权限管理系统**

用户 · 角色 · 菜单 · 权限 · 动态路由 · JWT · Redis 缓存

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen?style=flat-square\&logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.5.40-4FC08D?style=flat-square\&logo=vuedotjs)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square\&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-Redisson%203.45-DC382D?style=flat-square\&logo=redis)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?style=flat-square\&logo=springsecurity)

</div>

---

## 📖 项目简介

一套基于 **Spring Boot 3 + Vue 3** 的前后端分离 RBAC 权限管理系统。

采用 **用户 → 角色 → 菜单** 的权限模型，实现登录认证、动态路由、接口鉴权与按钮级权限控制，并通过 **Redis** 缓存用户菜单树与权限码，解决权限查询的多表关联性能问题。

> 个人学习实践项目，用于学习 Spring Boot、Spring Security、MyBatis、Vue 3、Redis 及 RBAC 权限设计。

---

## ✨ 功能特性

| 模块 | 功能 |
| --- | --- |
| 👤 用户管理 | 增删改查、分页查询、角色分配 |
| 🔐 角色管理 | 增删改查、菜单权限分配、权限变更审计 |
| 📋 菜单管理 | 树形菜单、增删改查、动态路由数据源 |
| 🔑 登录认证 | JWT 登录、Token 校验、401 拦截 |
| 🛡️ 权限控制 | 接口 `@PreAuthorizePerm` + 按钮 `v-has-perm` |
| 🧭 动态路由 | 按用户权限动态生成前端路由 |
| ⚡ 权限缓存 | Redis 缓存菜单树与权限码，变更后自动失效 |
| 📝 日志系统 | 操作日志 + 权限变更审计日志（`sys_permission_log`） |

---

## 🛠️ 技术栈

**后端**

- Spring Boot 3.5.9 · Spring Security 6.x
- MyBatis 3.0 · PageHelper 1.4.7 · Druid 1.2.23
- JJWT 0.11.5 · Lombok
- **Redis + Redisson 3.45.0**（Spring Data Redis / StringRedisTemplate）
- MySQL 8.0 · JDK 21 · Maven 3.9

**前端**

- Vue 3.5.40 · Vite · Pinia · Vue Router 5
- Element Plus · VxeUI · Axios

---

## 📁 项目结构

```text
RBAC/
├── springboot/                 # Spring Boot 后端
│   ├── src/main/java/com/fast/system/
│   │   ├── controller/         # 控制器（登录、用户、角色、菜单）
│   │   ├── service/            # 业务层（含 IPermCacheService）
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── domain/             # 实体与 VO（RouterVo、UserPermCacheVO）
│   │   ├── aspect/             # 权限校验、权限变更审计
│   │   ├── configure/          # Security、JWT 过滤器
│   │   └── utils/              # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml     # 数据源 / Redis / JWT 配置
│   │   └── mapper/             # MyBatis XML
│   └── pom.xml
│
├── vue/                        # Vue 3 前端
│   ├── src/
│   │   ├── api/  components/  router/  stores/  utils/  views/
│   │   ├── directive/hasPerm.js
│   │   └── permission.js
│   ├── vite.config.js
│   └── package.json
│
└── README.md
```

---

## ⚡ Redis 权限缓存设计

原先每次 `getInfo` / `getRouters` 都要多表关联查询菜单，改为缓存后只查一次库。

| 项 | 设计 |
| --- | --- |
| Key | `perm:user:{userId}` |
| 内容 | 菜单树 `routers` + 权限码 `perms`（JSON 序列化） |
| TTL | 18000s（与 JWT 有效期一致）+ 随机 ±300s |
| 读取 | 先查缓存，命中直接返回；未命中查库回写（Redisson 互斥锁防击穿） |
| 穿透 | 空结果缓存 60s |
| 更新 | 权限/菜单/用户变更时「先更新数据库，事务提交后删缓存」 |

实测效果：连续两次 `getInfo`，第 1 次触发缓存重建（3 条 SQL），第 2 次仅剩 1 条用户基础信息查询，菜单与权限 SQL 全部命中缓存。

---

## 🚀 快速开始

### 环境要求

| 工具 | 版本 |
| --- | --- |
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |

### 后端

`application.yml` 中数据库密码、Redis 连接、JWT 密钥均通过环境变量注入：

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

后端默认端口 `8080`，启动前先修改 `application.yml` 中的 Redis 地址与密码，并确保 MySQL 已创建 `fast` 库。

### 前端

```bash
cd vue
npm install
npm run dev
```

访问控制台输出的前端地址，使用 `admin` 账号登录（本地初始密码 `admin`）。

---

## 🔐 核心模块

| 模块 | 关键实现 |
| --- | --- |
| JWT 认证 | `TokenService`、`JwtAuthenticationTokenFilter` |
| 接口鉴权 | `@PreAuthorizePerm` + `PreAuthorizeAspect` |
| 权限缓存 | `IPermCacheService`、`PermCacheServiceImpl`（Redisson + StringRedisTemplate） |
| 权限审计 | `PermissionChangeAspect`、`sys_permission_log` |
| 动态路由 | `RouterVo`、`getRouters`、`routeStore.js` |
| 按钮权限 | `v-has-perm`、`hasPerm.js` |
| 分页查询 | `BaseController.startPage()` |
| 树形菜单 | `buildMenuTree()`、TreeSelect |

---

## 📦 部署

### 后端

```bash
cd springboot
mvn clean package -DskipTests
java -jar target/*.jar
```

### 前端

```bash
cd vue
npm run build
```

将生成的 `dist/` 部署到 Nginx，接口通过代理指向后端 `8080`。

---

<div align="center">

**Spring Boot × Vue 3 × RBAC × Redis**

</div>
