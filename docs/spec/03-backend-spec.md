# MKM Backend 规范说明

## 1. 文档目标

本文档定义 MKM 后端服务的职责范围、技术约束、接口规范、数据模型、安全要求和验收标准。

---

# 2. Backend 定位

Backend 是 MKM 的云端数据和服务层，负责：

```text
用户账号注册登录
文档云存储与同步
Todo 云端存储
公开知识库服务
评论和点赞
AI Provider 配置管理
AI 请求代理转发
```

Backend 不负责：

```text
本地仓库管理（由 Android 客户端负责）
Markdown 渲染（由客户端负责）
AI 模型直接训练或本地部署
```

---

# 3. 技术栈规范

## 3.1 必选技术

```text
Spring Boot 3.x
Kotlin
Spring Security 6
JWT（jjwt 0.12.6）
Spring Data JPA
PostgreSQL
BCrypt
```

---

## 3.2 版本约束

| 依赖 | 版本 |
|---|---|
| Spring Boot | 3.3.x |
| Kotlin | 1.9.x |
| jjwt | 0.12.6 |
| PostgreSQL Driver | 最新稳定 |
| Java Runtime | 17 |

---

# 4. 项目结构规范

```text
backend/src/main/kotlin/com/mkm/
├── config/
│   ├── SecurityConfig.kt
│   └── WebConfig.kt
├── controller/
│   ├── AuthController.kt
│   ├── DocumentController.kt
│   ├── TodoController.kt
│   ├── AiController.kt
│   ├── UserController.kt
│   └── GlobalExceptionHandler.kt
├── service/
│   ├── UserService.kt
│   ├── DocumentService.kt
│   ├── TodoService.kt
│   ├── AiProviderService.kt
│   └── AiService.kt
├── repository/
│   ├── UserRepository.kt
│   ├── DocumentRepository.kt
│   ├── TodoRepository.kt
│   └── AiProviderConfigRepository.kt
├── model/
│   ├── User.kt
│   ├── Document.kt
│   ├── Todo.kt
│   └── AiProviderConfig.kt
├── dto/
│   ├── AuthDtos.kt
│   ├── DocumentDtos.kt
│   ├── TodoDtos.kt
│   └── AiDtos.kt
├── security/
│   ├── JwtTokenProvider.kt
│   └── JwtAuthFilter.kt
└── MkmApplication.kt
```

---

# 5. 数据库规范

## 5.1 PostgreSQL

```text
开发数据库：mkm_db
用户名：mkm
密码：从环境变量注入
```

## 5.2 建表原则

```text
主键：UUID 或 BIGINT GENERATED ALWAYS AS IDENTITY
时间字段：timestamptz（带时区）
软删除：V1 可以先用物理删除
禁止保存明文密码
禁止保存明文 API Key
```

## 5.3 核心数据表

```text
users
documents
todos
ai_provider_configs
ai_conversations
ai_messages
ai_call_logs
comments
likes
```

---

# 6. API 规范

## 6.1 URL 规范

```text
所有 API 以 /api 开头
资源命名使用复数名词
嵌套资源最多两层
```

示例：

```text
/api/auth/register
/api/auth/login
/api/documents
/api/documents/{id}
/api/documents/public
/api/todos
/api/todos/{id}
/api/ai/providers
/api/ai/chat
/api/users/me
```

---

## 6.2 HTTP 方法规范

| 操作 | HTTP Method |
|---|---|
| 创建 | POST |
| 获取列表 | GET |
| 获取单条 | GET |
| 修改 | PUT 或 PATCH |
| 删除 | DELETE |

---

## 6.3 响应格式规范

成功响应直接返回数据体或数组，不强制包一层 `{code, data, message}` wrapper。

HTTP 状态码含义：

| 状态码 | 含义 |
|---|---|
| 200 | 查询、修改成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 / Token 无效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 冲突（如用户名已存在） |
| 500 | 服务器内部错误 |

错误响应格式：

```json
{
  "error": "错误类型",
  "message": "错误描述"
}
```

---

## 6.4 认证规范

除以下路径外，所有接口必须携带 Bearer Token：

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/documents/public
GET  /api/documents/public/{id}
GET  /api/documents/{id}/comments
```

Token 格式：

```text
Authorization: Bearer <jwt_token>
```

---

# 7. 权限规范

每个资源操作必须校验 owner：

```text
文档：只有 owner 可以增删改
Todo：只有 owner 可以增删改
AI Provider 配置：只有 owner 可以增删改
评论：只有评论作者可以删除
公开文档：所有人可读
私有文档：仅 owner 可读
```

---

# 8. 用户模块规范

## 8.1 注册

接口：`POST /api/auth/register`

要求：

```text
用户名唯一
邮箱唯一
密码 BCrypt 加密存储
注册成功返回 JWT Token
```

---

## 8.2 登录

接口：`POST /api/auth/login`

要求：

```text
支持用户名登录
密码 BCrypt 验证
登录成功返回 JWT Token
失败返回 401
```

---

## 8.3 获取当前用户

接口：`GET /api/users/me`

要求：

```text
返回当前登录用户信息
不返回密码字段
```

---

# 9. 文档模块规范

## 9.1 文档 CRUD

必须实现：

```text
GET    /api/documents         获取当前用户文档列表
POST   /api/documents         创建文档
GET    /api/documents/{id}    获取文档详情
PUT    /api/documents/{id}    更新文档
DELETE /api/documents/{id}    删除文档
GET    /api/documents/public  获取公开文档列表
```

---

## 9.2 文档字段

```text
id
title
content（TEXT，允许大文本）
isPublic
tags（数组，存储为 JSON 或 TEXT）
ownerId
createdAt
updatedAt
```

---

## 9.3 文档排序与分页

公开文档列表必须支持：

```text
分页：page / pageSize
搜索：q=标题关键词
标签筛选：tag=kotlin
排序：按 updatedAt 倒序
```

---

# 10. Todo 模块规范

## 10.1 Todo CRUD

必须实现：

```text
GET    /api/todos              获取当前用户 Todo 列表
POST   /api/todos              创建 Todo
PUT    /api/todos/{id}         更新 Todo
DELETE /api/todos/{id}         删除 Todo
POST   /api/todos/batch        批量创建（AI 提取待办用）
PUT    /api/todos/{id}/complete    标记完成
PUT    /api/todos/{id}/uncomplete  取消完成
```

---

## 10.2 Todo 字段

```text
id
title
note
priority: LOW / MEDIUM / HIGH
dueDate
completed
completedAt
sourceDocumentId
sourceDocumentTitle
ownerId
createdAt
updatedAt
```

---

# 11. AI 模块规范

## 11.1 AI Provider 配置

必须实现：

```text
GET    /api/ai/providers           获取用户 Provider 列表
POST   /api/ai/providers           添加 Provider
PUT    /api/ai/providers/{id}      更新 Provider
DELETE /api/ai/providers/{id}      删除 Provider
POST   /api/ai/providers/{id}/test 测试连接
```

---

## 11.2 API Key 安全规范

必须遵守：

```text
数据库不保存明文 API Key
使用环境变量 AI_KEY_ENCRYPTION_SECRET 对称加密
返回给前端时只返回脱敏结果：sk-abc****789
禁止日志打印 API Key
禁止在错误信息中暴露 API Key
用户可以删除配置
```

---

## 11.3 AI 功能接口

必须实现：

```text
POST /api/ai/chat                  普通提问
POST /api/ai/markdown/generate     生成 Markdown
POST /api/ai/document/summarize    文档总结
POST /api/ai/todo/extract          提取待办
```

---

## 11.4 AI 请求日志规范

ai_call_logs 表只保存：

```text
userId
providerId
feature（CHAT / MARKDOWN_GENERATE / SUMMARIZE / TODO_EXTRACT）
status（SUCCESS / FAILED）
durationMs
createdAt
```

禁止保存：

```text
prompt 内容
API Key
完整请求体
完整响应体
```

---

# 12. 评论和点赞规范

## 12.1 评论

```text
GET  /api/documents/{id}/comments    获取评论列表
POST /api/documents/{id}/comments    发表评论（需登录）
DELETE /api/documents/{id}/comments/{commentId}  删除评论（仅作者）
```

---

## 12.2 点赞

```text
POST   /api/documents/{id}/like    点赞（需登录）
DELETE /api/documents/{id}/like    取消点赞（需登录）
```

---

# 13. CORS 规范

开发环境允许的前端源：

```text
http://localhost:5173   Vue Web 开发
http://10.0.2.2:8080    Android 模拟器（后端本地）
```

生产环境：

```text
Web 部署域名
```

---

# 14. 安全规范

必须满足：

```text
密码 BCrypt 加密
JWT 过期时间：建议 24 小时，刷新 Token 后续支持
所有资源必须校验 owner
公开文档以外的资源需 JWT 鉴权
接口限流（注册、登录、AI 调用接口）
API Key 加密保存
日志不打印敏感字段
```

---

# 15. 环境变量规范

生产环境必须配置：

```text
DB_PASSWORD=强密码
JWT_SECRET=至少32位随机字符串
AI_KEY_ENCRYPTION_SECRET=至少32位随机字符串
SPRING_PROFILES_ACTIVE=prod
```

开发环境可使用 `application-dev.yml` 配置，不得提交到 Git。

---

# 16. 部署规范

## 16.1 Docker

后端必须提供 Dockerfile，包含：

```text
多阶段构建（build + run）
JVM 参数配置
8080 端口对外
健康检查接口：GET /api/actuator/health
```

---

## 16.2 Nginx

Backend 通过 Nginx 反向代理：

```text
/api/ → Spring Boot :8080
/     → Vue Web 静态资源
```

---

# 17. Backend 验收标准

V1 Backend 必须通过以下验证：

```text
POST /api/auth/register    注册成功，返回 Token
POST /api/auth/login       登录成功，返回 Token
POST /api/documents        创建文档成功
GET  /api/documents        获取文档列表
GET  /api/documents/public 获取公开文档列表
POST /api/todos            创建 Todo
POST /api/ai/providers     添加 AI Provider 配置
POST /api/ai/chat          AI 提问返回结果
POST /api/documents/{id}/comments  发表评论
```
