# 🚀 RBAC 权限管理系统

**基于 Spring Boot 3 + Vue 3 的前后端分离 RBAC 权限管理系统**

用户 · 角色 · 菜单 · 权限 · 动态路由 · JWT

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen?style=flat-square\&logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.5.40-4FC08D?style=flat-square\&logo=vuedotjs)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square\&logo=mysql)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?style=flat-square\&logo=springsecurity)
</div>

---

## 📖 项目简介

本项目是一套基于 **Spring Boot 3 + Vue 3** 的前后端分离 RBAC 权限管理系统。

采用 **用户 → 角色 → 菜单** 的权限模型，实现用户认证、角色管理、菜单权限、动态路由等功能。

> 个人学习实践项目，主要用于学习 Spring Boot、Spring Security、MyBatis、Vue 3 及 RBAC 权限设计。

---

## ✨ 功能特性

| 模块         | 功能                     |
| ---------- | ---------------------- |
| 👤 用户管理    | 增删改查、分页查询、角色分配         |
| 🔐 角色管理    | 增删改查、菜单权限分配            |
| 📋 菜单管理    | 树形菜单、增删改查、图标选择         |
| 🔑 登录认证    | JWT 登录、Token 校验、401 拦截 |
| 🛡️ 权限控制   | Spring Security 认证与授权  |
| 🧭 动态路由    | 根据用户权限动态生成路由           |
| 👨‍💻 个人中心 | 信息、密码、头像管理             |
| 📄 分页查询    | PageHelper 分页          |
| 📝 日志系统    | 异步日志、按天滚动、错误日志独立记录     |

---

## 🛠️ 技术栈

### 后端

* Spring Boot 3.5.9
* Spring Security 6.x
* MyBatis 3.0
* JJWT 0.11.5
* PageHelper 1.4.7
* Druid 1.2.23
* MySQL 8.0
* Lombok

### 前端

* Vue 3.5.40
* Vite 8.1.5
* Pinia 4.0.2
* Vue Router 5.2.0
* Element Plus
* VxeUI
* Axios

---

## 📁 项目结构

```text
rbac-permission-system/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/fast/system/
│   │   ├── config/             # 配置
│   │   ├── controller/         # 控制器
│   │   ├── domain/             # 实体类、VO
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── service/            # 业务层
│   │   ├── exception/          # 异常处理
│   │   └── utils/              # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── logback.xml
│   │   └── mapper/
│   └── pom.xml
│
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── router/
│   │   ├── stores/
│   │   ├── utils/
│   │   └── views/
│   ├── .env.development
│   ├── .env.production
│   ├── vite.config.js
│   ├── package.json
│   └── index.html
│
├── docs/
└── README.md
```

---

## 🚀 快速开始

### 环境要求

| 工具      | 版本   |
| ------- | ---- |
| JDK     | 17+  |
| Maven   | 3.8+ |
| Node.js | 18+  |
| MySQL   | 8.0+ |

## 🔐 核心模块

| 模块     | 关键实现                                           |
| ------ | ---------------------------------------------- |
| JWT 认证 | `TokenService`、`JwtAuthenticationTokenFilter`  |
| 权限拦截   | `SecurityConfig`、`JwtAuthenticationEntryPoint` |
| 动态路由   | `RouterVo`、`routeStore.js`                     |
| 分页查询   | `BaseController.startPage()`                   |
| 树形菜单   | `buildMenuTree()`、`TreeSelect`                 |
| 请求封装   | `utils/request.js`                             |
| 状态管理   | `userStore.js`、`routeStore.js`                 |

---

## 📦 部署

### 后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/*.jar
```

### 前端

```bash
cd frontend
npm run build
```

将生成的 `dist/` 部署到 Nginx。

---

<div align="center">

**Spring Boot × Vue 3 × RBAC**

⭐ 如果项目对你有帮助，欢迎 Star！

</div>
