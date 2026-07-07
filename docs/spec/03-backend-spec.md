# MKM Backend 规范说明

## 1. 文档目标

本文档定义 MKM 后端服务的职责范围、技术约束、接口规范、数据模型、安全要求和验收标准。

---

# 2. Backend 定位

Backend 是 MKM 的云端数据和服务层，负责：

```text
用户账号注册登录
文档云存储与同步（含文件夹结构）
附件二进制存储（≤10MB）
Todo 云端存储
AI Provider 配置管理
AI 请求代理转发
AI 多会话历史云端存储
```

Backend 不负责：

```text
本地知识库管理（由 Android 客户端负责）
Markdown 渲染（由客户端负责）
AI 模型直接训练或本地部署
社区评论/点赞（V1 不做）
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

## 3.3 开发环境建议

| 项目 | 推荐版本 / 工具 | 说明 |
|---|---|---|
| JDK | **17 LTS** | 与 Android 共用，`build.gradle.kts` toolchain 使用 17 |
| IDE | IntelliJ IDEA / Android Studio | Kotlin + Spring Boot 开发 |
| Gradle | 使用项目自带 `./gradlew` | 不要求全局安装 Gradle |
| PostgreSQL | **16** | 开发环境通过 Docker Compose 启动 |
| Docker | Docker Desktop for Mac | 用于运行 PostgreSQL |
| API 调试 | Postman / Insomnia / curl | 调试 REST API |

详细安装说明见：`docs/deployment/00-dev-environment.md`。

---

## 3.4 核心依赖清单

```text
spring-boot-starter-web          REST API
spring-boot-starter-security     Spring Security 6
spring-boot-starter-data-jpa     JPA / Hibernate
spring-boot-starter-validation   参数校验
jackson-module-kotlin            Kotlin JSON 序列化
kotlin-reflect                   Spring Kotlin 反射支持
jjwt-api / impl / jackson 0.12.6 JWT
postgresql                       PostgreSQL JDBC Driver
spring-boot-starter-test         测试框架
spring-security-test             Security 测试工具
```

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
│   ├── AttachmentController.kt
│   ├── TodoController.kt
│   ├── AiController.kt
│   ├── UserController.kt
│   └── GlobalExceptionHandler.kt
├── service/
│   ├── UserService.kt
│   ├── DocumentService.kt
│   ├── AttachmentService.kt
│   ├── TodoService.kt
│   ├── AiProviderService.kt
│   └── AiService.kt
├── repository/
│   ├── UserRepository.kt
│   ├── DocumentRepository.kt
│   ├── AttachmentRepository.kt
│   ├── TodoRepository.kt
│   ├── AiProviderConfigRepository.kt
│   ├── AiConversationRepository.kt
│   └── AiMessageRepository.kt
├── model/
│   ├── User.kt
│   ├── Document.kt
│   ├── Attachment.kt
│   ├── Todo.kt
│   ├── AiProviderConfig.kt
│   ├── AiConversation.kt
│   └── AiMessage.kt
├── dto/
│   ├── AuthDtos.kt
│   ├── DocumentDtos.kt
│   ├── AttachmentDtos.kt
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
主键：BIGSERIAL（自增 BIGINT）
时间字段：TIMESTAMP
软删除：V1 使用物理删除
禁止保存明文密码
禁止保存明文 API Key
附件数据使用 BYTEA 存储
```

## 5.3 核心数据表

```text
users
documents           含文件夹路径、同步状态、本地修改时间
attachments         附件二进制（≤10MB）
todos
ai_provider_configs
ai_conversations
ai_messages
ai_call_logs
```

---

# 6. API 规范

## 6.1 URL 规范

```text
所有 API 以 /api 开头
资源命名使用复数名词
嵌套资源最多两层
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
| 413 | 附件超过 10MB |
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
附件：只有 owner 可以增删改
Todo：只有 owner 可以增删改
AI Provider 配置：只有 owner 可以增删改
AI 会话/消息：只有 owner 可以增删改
```

V1 不做公开文档、社区权限。

---

# 8. 用户模块规范

## 8.1 注册

接口：`POST /api/auth/register`

要求：

```text
用户名唯一
邮箱唯一（可选）
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

## 8.3 获取/更新当前用户

接口：`GET /api/users/me`、`PATCH /api/users/me`

要求：

```text
获取：返回当前登录用户信息，不返回密码字段
更新：支持修改 nickname、avatar_url
```

---

# 9. 文档模块规范

## 9.1 文档 CRUD

必须实现：

```text
GET    /api/documents               获取当前用户文档列表（支持搜索/标签筛选/文件夹路径筛选）
POST   /api/documents               创建文档
GET    /api/documents/{id}          获取文档详情
PUT    /api/documents/{id}          更新文档
DELETE /api/documents/{id}          删除文档
```

---

## 9.2 文档字段

```text
id
title
content（TEXT，允许大文本）
folderPath（文件夹路径）
fileName
tags（逗号分隔字符串）
syncEnabled（是否开启云端同步）
localUpdatedAt（本地修改时间，用于冲突检测）
ownerId
createdAt
updatedAt
```

---

## 9.3 冲突检测

更新文档时：

```text
客户端传入 localUpdatedAt
服务端对比 localUpdatedAt 和当前云端 updatedAt
若两者都有变更（本地 != 云端）→ 返回 409 Conflict + 云端当前版本
客户端弹窗让用户选择保留本地或云端
```

---

## 9.4 文档列表查询参数

```text
?q=          全文搜索（文件名 + 标签 + 正文）
?tag=        标签筛选
?folder=     文件夹路径筛选
?page=       分页
?pageSize=   分页大小
?sort=       排序（updatedAt DESC 默认）
```

---

# 10. 附件模块规范

## 10.1 附件 API

必须实现：

```text
POST   /api/documents/{id}/attachments        上传附件（multipart/form-data）
GET    /api/documents/{id}/attachments        获取文档附件列表
GET    /api/attachments/{id}                  下载/获取附件数据
DELETE /api/attachments/{id}                  删除附件
GET    /api/documents/{id}/attachments/resolve?path=   根据相对路径解析附件
```

## 10.2 附件规范

```text
单文件最大 10MB，超出返回 413
存储方式：PostgreSQL BYTEA
相对路径解析：/api/documents/{id}/attachments/resolve?path=./images/a.png
渲染时后端把相对路径解析为下载 URL，不修改原始 Markdown 内容
```

---

# 11. Todo 模块规范

## 11.1 Todo CRUD

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

## 11.2 Todo 字段

```text
id
title
note
priority: LOW / MEDIUM / HIGH
dueDate
completed
completedAt
sourceType: manual / ai
sourceDocumentId
sourceDocumentTitle
ownerId
createdAt
updatedAt
```

---

# 12. AI 模块规范

## 12.1 AI Provider 配置

必须实现：

```text
GET    /api/ai/providers           获取用户 Provider 配置
POST   /api/ai/providers           添加 Provider
PUT    /api/ai/providers/{id}      更新 Provider
DELETE /api/ai/providers/{id}      删除 Provider
POST   /api/ai/providers/{id}/test 测试连接
```

V1 每个用户只支持一个 Provider 配置（OpenAI 兼容）。

---

## 12.2 API Key 安全规范

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

## 12.3 AI 功能接口

必须实现：

```text
POST /api/ai/chat                  普通提问
POST /api/ai/markdown/generate     生成 Markdown
POST /api/ai/document/summarize    文档总结
POST /api/ai/todo/extract          提取待办
```

---

## 12.4 AI 会话管理接口

必须实现：

```text
GET    /api/ai/conversations                    获取会话列表
POST   /api/ai/conversations                    新建会话
GET    /api/ai/conversations/{id}/messages      获取会话消息记录
DELETE /api/ai/conversations/{id}               删除会话
```

---

## 12.5 AI 请求日志规范

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
JWT 过期时间：建议 24 小时
所有资源必须校验 owner
接口限流（注册、登录、AI 调用接口）
API Key 加密保存
日志不打印敏感字段
附件大小限制 10MB，超出返回 413
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
POST /api/auth/register              注册成功，返回 Token
POST /api/auth/login                 登录成功，返回 Token
POST /api/documents                  创建文档成功
GET  /api/documents                  获取文档列表
PUT  /api/documents/{id}             更新文档，冲突时返回 409
POST /api/documents/{id}/attachments 上传附件成功
GET  /api/documents/{id}/attachments/resolve?path= 相对路径解析成功
POST /api/todos                      创建 Todo
POST /api/ai/providers               添加 AI Provider 配置
POST /api/ai/chat                    AI 提问返回结果
GET  /api/ai/conversations           获取会话列表
```
