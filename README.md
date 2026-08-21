<div align="center">

# 🚀 RBAC 权限管理系统

**基于 Spring Boot 3 + Vue 3 的前后端分离权限管理系统**

用户 · 角色 · 菜单 · 权限 · 动态路由 · JWT

<br>

<img src="https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen?style=flat-square&logo=springboot">
<img src="https://img.shields.io/badge/Vue-3.5.40-4FC08D?style=flat-square&logo=vuedotjs">
<img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql">
<img src="https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?style=flat-square&logo=springsecurity">
<img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square">

</div>

---

## 📖 项目简介

这是一个基于 **Spring Boot 3 + Vue 3** 开发的前后端分离 RBAC 权限管理系统。

项目主要用于学习和实践：

* Spring Boot
* Spring Security
* JWT
* MyBatis
* MySQL
* Vue 3
* Vue Router
* Pinia
* 前后端分离
* RBAC 权限模型

系统采用 **用户 → 角色 → 菜单** 的权限管理方式，实现不同用户登录后拥有不同的菜单和访问权限。

---

## ✨ 功能

| 模块         | 功能                     |
| :--------- | :--------------------- |
| 👤 用户管理    | 用户增删改查、分页、角色分配         |
| 🔐 角色管理    | 角色增删改查、菜单权限分配          |
| 📋 菜单管理    | 菜单树、增删改查、图标选择          |
| 🔑 登录认证    | JWT 登录、Token 校验、401 处理 |
| 🛡️ 权限控制   | Spring Security 认证与授权  |
| 🧭 动态路由    | 根据用户权限动态生成路由           |
| 👨‍💻 个人中心 | 修改个人信息、密码、头像           |
| 📄 分页查询    | PageHelper 分页          |
| 📝 日志      | 日志记录及滚动                |

---

## 🛠️ 技术栈

### 后端

* Spring Boot 3.5.9
* Spring Security 6.x
* JJWT 0.11.5
* MyBatis
* PageHelper
* Druid
* Lombok
* Maven

### 前端

* Vue 3.5.40
* Vite
* Vue Router
* Pinia
* Element Plus
* VxeUI
* Axios

### 数据库

* MySQL 8.0

---

## 📁 项目结构

```text
rbac-permission-system/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/fast/system/
│   │       ├── config/      # 配置
│   │       ├── controller/  # Controller
│   │       ├── domain/      # 实体类
│   │       ├── mapper/      # MyBatis Mapper
│   │       ├── service/     # 业务层
│   │       ├── exception/   # 异常处理
│   │       └── utils/       # 工具类
│   ├── src/main/resources/
│   └── pom.xml
│
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/             # API
│   │   ├── components/      # 公共组件
│   │   ├── router/          # 路由
│   │   ├── stores/          # Pinia
│   │   ├── utils/           # 工具
│   │   └── views/           # 页面
│   ├── package.json
│   └── vite.config.js
│
├── docs/                    # 项目文档
├── README.md
└── .gitignore
```

---

## 🚀 快速开始

### 环境要求

* JDK 17+
* Maven 3.8+
* Node.js 18+
* MySQL 8.0+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/rbac-permission-system.git
cd rbac-permission-system
```

### 2. 创建数据库

```sql
CREATE DATABASE fast
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

然后执行项目数据库 SQL。

### 3. 配置数据库

修改：

```text
backend/src/main/resources/application.yml
```

配置 MySQL 用户名和密码。

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:90
```

---

## 📚 项目说明

本项目为个人学习实践项目，开发过程中参考了 **B 站相关技术教程**，主要用于学习 Spring Boot、Spring Security、Vue 3 以及 RBAC 权限管理。

**本项目仅用于学习和技术交流，不用于商业用途。**

---

## 📄 License

本项目采用 **MIT License**。

---

<div align="center">

**Spring Boot × Vue 3 × RBAC**

⭐ 如果项目对你有帮助，欢迎 Star！

</div>
