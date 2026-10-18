# Stride · 个人跑步训练管理系统

一个面向个人跑者的训练管理平台，用于记录每次跑步、自动计算配速、按周期统计跑量趋势，
并支持记录跑步时听的音乐、生成歌曲排行榜。

> 本项目为本科毕业设计《个人跑步锻炼管理系统的设计与实现》的工程实现，
> 采用前后端分离架构，后端 Spring Boot 3 + MyBatis-Plus，前端 Vue 3 + Vite。

---

## 功能

### 已实现

| 模块 | 功能 |
|---|---|
| 认证 | 注册、登录、JWT 无状态认证、密码 BCrypt 加密存储 |
| 权限 | 基于角色的访问控制（管理员 / 普通用户），接口级鉴权 |
| 用户 | 查看个人信息、管理员管理用户列表 |

### 开发中

- 跑步记录管理：记录距离、时长、日期、跑步类型，自动计算配速
- 数据统计：月跑量、总跑量、平均配速、跑量趋势
- 音乐陪伴：记录跑步时听的歌，按歌曲统计被听次数生成排行榜

---

## 技术栈

| 层次 | 技术 |
|---|---|
| 语言 / 运行时 | Java 17 |
| 构建 | Maven |
| Web 框架 | Spring Boot 3.2.5 |
| 持久层 | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 |
| 认证 | JJWT 0.11.5 |
| 密码加密 | BCrypt（spring-security-crypto） |
| 前端 | Vue 3 + Vite + Vue Router + Pinia |
| UI 组件 | Element Plus |
| HTTP 客户端 | Axios |

---

## 项目结构

```
stride/
├── backend/                                  Spring Boot 后端
│   ├── src/main/java/com/stride/
│   │   ├── StrideApplication.java            启动类
│   │   ├── common/Result.java                统一响应体
│   │   ├── config/WebConfig.java             拦截器注册、CORS 配置
│   │   ├── controller/UserController.java    接口层
│   │   ├── service/                          业务层（用户、认证、密码）
│   │   ├── mapper/UserMapper.java            数据访问层
│   │   ├── entity/User.java                  数据库实体
│   │   ├── vo/                               返回给前端的对象（不含敏感字段）
│   │   ├── exception/                        业务异常与全局异常处理
│   │   ├── filter/JwtRequestFilter.java      JWT 认证 + 角色鉴权拦截器
│   │   └── util/JwtUtil.java                 Token 签发与解析
│   └── src/main/resources/application.properties
└── frontend/                                 Vue 3 前端
    └── src/
        ├── router/index.js                   路由与全局守卫
        ├── stores/                           Pinia 状态管理（认证、用户）
        ├── utils/request.js                  Axios 封装（统一注入 Token、统一错误处理）
        └── views/                            页面（登录、注册、个人主页、用户管理）
```

---

## 快速开始

### 环境要求

| 组件 | 版本 |
|---|---|
| JDK | 17 或更高 |
| Node.js | 18 或更高 |
| MySQL | 8.x |

### 1. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS stride DEFAULT CHARSET utf8mb4;
USE stride;

CREATE TABLE users (
  id       INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50)  NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  email    VARCHAR(100),
  role     INT DEFAULT 2
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

> `role` 字段含义：`1` = 管理员，`2` = 普通用户

### 2. 配置后端

数据库密码等敏感信息通过环境变量注入，**不要写进代码**。
也可以复制 `backend/src/main/resources/application-local.properties.example`
为 `application-local.properties` 并填入真实值（该文件已被 `.gitignore` 排除）。

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `DB_HOST` | `localhost` | 数据库地址 |
| `DB_PORT` | `3306` | 数据库端口 |
| `DB_NAME` | `stride` | 数据库名 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | （空） | 数据库密码 |
| `JWT_SECRET` | 开发默认值 | **生产环境必须替换** |

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端运行在 http://localhost:8081

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 http://localhost:5173，访问 http://localhost:5173/#/login 即可。

### 5. 创建管理员账号

注册接口创建的账号默认为普通用户（`role = 2`），
需要管理员权限时手动调整：

```sql
UPDATE users SET role = 1 WHERE username = '你的用户名';
```

---

## 接口说明

所有接口统一返回：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/users/register` | 注册 | 公开 |
| POST | `/api/users/login` | 登录，返回 Token 与用户信息 | 公开 |
| GET | `/api/users/me` | 查询当前登录用户 | 需登录 |
| GET | `/api/users` | 查询全部用户 | **仅管理员** |
| PUT | `/api/users/{id}` | 修改用户信息 | **仅管理员** |
| DELETE | `/api/users/{id}` | 删除用户 | **仅管理员** |

需要认证的接口请在请求头携带：

```
Authorization: Bearer <token>
```

### 状态码约定

| 状态码 | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 请求参数有误 |
| 401 | 未登录或登录已过期 |
| 403 | 已登录但无权限 |
| 404 | 资源不存在 |
| 409 | 数据冲突（如用户名已存在） |
| 500 | 服务器内部错误 |

---

## 本地开发说明

### 测试账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 |
| tom | 123456 | 普通用户 |

### 构建

```bash
cd backend
mvn clean package        # 产出可执行 jar，位于 target/
```

### 关于敏感信息

数据库密码与 JWT 密钥均通过环境变量或 `application-local.properties` 注入，
`application-local.properties` 已在 `.gitignore` 中排除，不会被提交到仓库。

---

## 许可证

本项目为个人学习与毕业设计用途。
