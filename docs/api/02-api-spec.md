# MKM API 接口规范

## 1. 文档目标

本文档定义 MKM V1 所有 REST API 的完整规范，包括：

- 公共约定
- Auth 接口
- Document 接口
- Todo 接口
- AI 接口
- 请求/响应字段说明

---

## 2. 公共约定

## 2.1 Base URL

```text
http://localhost:8080/api
```

生产环境通过 Nginx 或云服务代理。

---

## 2.2 认证

所有非公开接口需要 JWT Bearer Token：

```http
Authorization: Bearer <token>
```

---

## 2.3 时间格式

所有时间字段使用 ISO 8601：

```text
2024-11-20T14:30:00
```

---

## 2.4 通用响应格式

### 成功 - 单对象

```json
{
  "id": 1,
  "title": "Spring Boot 学习笔记",
  ...
}
```

### 成功 - 列表

```json
[
  { "id": 1, "title": "..." },
  { "id": 2, "title": "..." }
]
```

### 失败

```json
{
  "error": "USER_NOT_FOUND",
  "message": "用户不存在"
}
```

---

## 2.5 HTTP 状态码约定

| 状态码 | 含义 |
|---|---|
| 200 | 成功 |
| 201 | 创建成功 |
| 204 | 删除成功（无响应体） |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务内部错误 |
| 504 | AI 请求超时 |

---

# 3. Auth 接口

## 3.1 注册

```http
POST /api/auth/register
```

### 请求

```json
{
  "username": "windmeta",
  "password": "password123",
  "email": "user@example.com"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | String | 是 | 2-64 字符，字母/数字/下划线 |
| password | String | 是 | 至少 6 位 |
| email | String | 否 | 邮箱格式 |

### 响应 201

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "windmeta"
}
```

### 错误

| 状态码 | 原因 |
|---|---|
| 400 | 用户名已存在、邮箱已注册、格式校验失败 |

---

## 3.2 登录

```http
POST /api/auth/login
```

### 请求

```json
{
  "username": "windmeta",
  "password": "password123"
}
```

### 响应 200

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "windmeta"
}
```

### 错误

| 状态码 | 原因 |
|---|---|
| 401 | 用户名或密码错误 |

---

## 3.3 获取当前用户信息（可选）

```http
GET /api/users/me
```

### 响应 200

```json
{
  "id": 1,
  "username": "windmeta",
  "email": "user@example.com",
  "avatarUrl": null,
  "bio": null,
  "createdAt": "2024-11-20T14:30:00"
}
```

---

# 4. Document 接口

## 4.1 获取我的文档列表

```http
GET /api/documents
```

### 查询参数（可选）

| 参数 | 类型 | 说明 |
|---|---|---|
| page | int | 页码，从 0 开始 |
| size | int | 每页数量，默认 20 |
| keyword | String | 标题搜索，V1 可选 |

### 响应 200

```json
[
  {
    "id": 1,
    "title": "Spring Boot 学习笔记",
    "summary": null,
    "isPublic": false,
    "tags": "spring,kotlin",
    "ownerUsername": "windmeta",
    "createdAt": "2024-11-20T14:30:00",
    "updatedAt": "2024-11-21T10:00:00"
  }
]
```

注意：列表接口不返回 `content` 字段，只返回元数据。

---

## 4.2 获取文档详情

```http
GET /api/documents/{id}
```

### 响应 200

```json
{
  "id": 1,
  "title": "Spring Boot 学习笔记",
  "content": "# Spring Boot 学习笔记\n\n## 背景\n\n...",
  "summary": null,
  "isPublic": false,
  "tags": "spring,kotlin",
  "ownerUsername": "windmeta",
  "createdAt": "2024-11-20T14:30:00",
  "updatedAt": "2024-11-21T10:00:00"
}
```

### 错误

| 状态码 | 原因 |
|---|---|
| 403 | 不是自己的文档且未公开 |
| 404 | 文档不存在 |

---

## 4.3 创建文档

```http
POST /api/documents
```

### 请求

```json
{
  "title": "Spring Boot 学习笔记",
  "content": "# Spring Boot 学习笔记\n\n...",
  "isPublic": false,
  "tags": "spring,kotlin"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| title | String | 是 | 1-256 字符 |
| content | String | 否 | Markdown 内容，默认空 |
| isPublic | Boolean | 否 | 默认 false |
| tags | String | 否 | 逗号分隔 |

### 响应 201

返回完整 DocumentDto。

---

## 4.4 更新文档

```http
PUT /api/documents/{id}
```

### 请求

```json
{
  "title": "Spring Boot 学习笔记（更新）",
  "content": "# 更新内容...",
  "isPublic": false,
  "tags": "spring"
}
```

### 响应 200

返回完整 DocumentDto。

### 错误

| 状态码 | 原因 |
|---|---|
| 403 | 不是自己的文档 |
| 404 | 文档不存在 |

---

## 4.5 删除文档

```http
DELETE /api/documents/{id}
```

### 响应 204

无响应体。

---

## 4.6 获取公开文档列表

```http
GET /api/documents/public
```

不需要认证。

### 响应 200

格式同文档列表，不包含 content。

---

# 5. Todo 接口

## 5.1 获取待办列表

```http
GET /api/todos
```

### 查询参数（可选）

| 参数 | 类型 | 说明 |
|---|---|---|
| completed | boolean | true / false 过滤 |

### 响应 200

```json
[
  {
    "id": 1,
    "title": "学习 Spring Security",
    "description": null,
    "completed": false,
    "priority": "medium",
    "dueDate": null,
    "sourceType": "manual",
    "sourceDocumentId": null,
    "createdAt": "2024-11-20T14:30:00",
    "updatedAt": "2024-11-20T14:30:00"
  }
]
```

---

## 5.2 创建待办

```http
POST /api/todos
```

### 请求

```json
{
  "title": "学习 Spring Security",
  "description": "完成 JWT 鉴权流程阅读",
  "priority": "medium",
  "dueDate": "2024-11-25T23:59:00",
  "sourceType": "manual",
  "sourceDocumentId": null
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| title | String | 是 | 待办标题 |
| description | String | 否 | 描述 |
| priority | String | 否 | low / medium / high，默认 medium |
| dueDate | DateTime | 否 | 截止时间 |
| sourceType | String | 否 | manual / document / ai |
| sourceDocumentId | Long | 否 | 来源文档 ID |

### 响应 201

返回完整 TodoDto。

---

## 5.3 批量创建待办

```http
POST /api/todos/batch
```

用于 AI 提取待办后批量写入。

### 请求

```json
{
  "todos": [
    { "title": "学习 Spring Security", "priority": "medium", "sourceType": "ai" },
    { "title": "完成 Android 登录页", "priority": "high", "sourceType": "ai" }
  ]
}
```

### 响应 201

```json
[
  { "id": 1, "title": "学习 Spring Security", ... },
  { "id": 2, "title": "完成 Android 登录页", ... }
]
```

---

## 5.4 更新待办

```http
PUT /api/todos/{id}
```

### 请求

```json
{
  "title": "学习 Spring Security",
  "completed": false,
  "priority": "high"
}
```

---

## 5.5 完成 / 取消完成待办

```http
PUT /api/todos/{id}/complete
PUT /api/todos/{id}/uncomplete
```

### 响应 200

返回更新后的 TodoDto。

---

## 5.6 删除待办

```http
DELETE /api/todos/{id}
```

### 响应 204

---

# 6. AI Provider 接口

## 6.1 获取 Provider 列表

```http
GET /api/ai/providers
```

### 响应 200

```json
[
  {
    "id": 1,
    "providerName": "DeepSeek",
    "providerType": "openai-compatible",
    "baseUrl": "https://api.deepseek.com",
    "modelName": "deepseek-chat",
    "maskedApiKey": "sk-abc****789",
    "enabled": true,
    "defaultProvider": true,
    "createdAt": "2024-11-20T14:30:00"
  }
]
```

---

## 6.2 创建 Provider

```http
POST /api/ai/providers
```

### 请求

```json
{
  "providerName": "DeepSeek",
  "providerType": "openai-compatible",
  "baseUrl": "https://api.deepseek.com",
  "modelName": "deepseek-chat",
  "apiKey": "sk-xxxxxxxxxxxx",
  "defaultProvider": true
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| providerName | String | 是 | 显示名 |
| providerType | String | 是 | openai-compatible |
| baseUrl | String | 是 | https 开头 |
| modelName | String | 是 | 模型名称 |
| apiKey | String | 是 | 原始 Key，后端加密保存 |
| defaultProvider | Boolean | 否 | 默认 false |

---

## 6.3 更新 Provider

```http
PUT /api/ai/providers/{id}
```

### 请求

```json
{
  "providerName": "DeepSeek Pro",
  "modelName": "deepseek-reasoner",
  "apiKey": null,
  "enabled": true
}
```

说明：

```text
apiKey 为 null 时不更新原有 Key。
apiKey 有值时重新加密保存。
```

---

## 6.4 删除 Provider

```http
DELETE /api/ai/providers/{id}
```

---

## 6.5 设为默认 Provider

```http
PUT /api/ai/providers/{id}/default
```

响应 200，返回更新后的 Provider。

---

## 6.6 测试 Provider

```http
POST /api/ai/providers/{id}/test
```

### 响应 200

```json
{
  "success": true,
  "message": "连接成功",
  "latencyMs": 1200
}
```

失败：

```json
{
  "success": false,
  "message": "Invalid API key"
}
```

---

# 7. AI 功能接口

## 7.1 普通提问

```http
POST /api/ai/chat
```

### 请求

```json
{
  "message": "Spring Security JWT 鉴权流程是什么？",
  "conversationId": null
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| message | String | 是 | 用户输入 |
| conversationId | Long | 否 | 继续已有对话，null 为新建 |

### 响应 200

```json
{
  "conversationId": 1,
  "messageId": 2,
  "content": "## JWT 鉴权流程\n\n...",
  "role": "assistant"
}
```

### 错误

| 状态码 | 原因 |
|---|---|
| 400 | 未配置 Provider |
| 504 | AI 服务超时 |

---

## 7.2 生成 Markdown

```http
POST /api/ai/markdown/generate
```

### 请求

```json
{
  "requirement": "Kotlin 协程学习笔记"
}
```

### 响应 200

```json
{
  "content": "# Kotlin 协程学习笔记\n\n## 1. 背景\n\n...",
  "suggestedTitle": "Kotlin 协程学习笔记"
}
```

---

## 7.3 文档总结

```http
POST /api/ai/documents/{id}/summarize
```

### 响应 200

```json
{
  "content": "## 核心摘要\n\n...\n\n## 可执行待办\n\n- [ ] ...",
  "extractedTodos": [
    { "title": "学习 Spring Security", "priority": "medium" },
    { "title": "整理 JWT 笔记", "priority": "low" }
  ]
}
```

---

## 7.4 提取待办

```http
POST /api/ai/text/todos
```

### 请求

```json
{
  "content": "这周需要学 Spring Security，完成 Android 登录页，整理 JWT 笔记"
}
```

### 响应 200

```json
{
  "content": "## 待办建议\n\n- [ ] 学习 Spring Security\n- [ ] 完成 Android 登录页\n- [ ] 整理 JWT 笔记",
  "todos": [
    { "title": "学习 Spring Security", "priority": "medium" },
    { "title": "完成 Android 登录页", "priority": "high" },
    { "title": "整理 JWT 笔记", "priority": "low" }
  ]
}
```

---

# 8. 接口总览

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| POST | /api/auth/register | 否 | 注册 |
| POST | /api/auth/login | 否 | 登录 |
| GET | /api/users/me | 是 | 当前用户信息 |
| GET | /api/documents | 是 | 我的文档列表 |
| GET | /api/documents/{id} | 视权限 | 文档详情 |
| POST | /api/documents | 是 | 创建文档 |
| PUT | /api/documents/{id} | 是 | 更新文档 |
| DELETE | /api/documents/{id} | 是 | 删除文档 |
| GET | /api/documents/public | 否 | 公开文档 |
| GET | /api/todos | 是 | 待办列表 |
| POST | /api/todos | 是 | 创建待办 |
| POST | /api/todos/batch | 是 | 批量创建待办 |
| PUT | /api/todos/{id} | 是 | 更新待办 |
| PUT | /api/todos/{id}/complete | 是 | 完成待办 |
| PUT | /api/todos/{id}/uncomplete | 是 | 取消完成 |
| DELETE | /api/todos/{id} | 是 | 删除待办 |
| GET | /api/ai/providers | 是 | Provider 列表 |
| POST | /api/ai/providers | 是 | 创建 Provider |
| PUT | /api/ai/providers/{id} | 是 | 更新 Provider |
| DELETE | /api/ai/providers/{id} | 是 | 删除 Provider |
| PUT | /api/ai/providers/{id}/default | 是 | 设为默认 |
| POST | /api/ai/providers/{id}/test | 是 | 测试连接 |
| POST | /api/ai/chat | 是 | 普通提问 |
| POST | /api/ai/markdown/generate | 是 | 生成 Markdown |
| POST | /api/ai/documents/{id}/summarize | 是 | 文档总结 |
| POST | /api/ai/text/todos | 是 | 提取待办 |

---

# 9. Android / Web DTO 建议

## Android Model

```kotlin
// 文档
data class DocumentSummary(val id: Long, val title: String, val tags: String?, val updatedAt: String)
data class DocumentDetail(val id: Long, val title: String, val content: String, val isPublic: Boolean, val tags: String?)
data class DocumentRequest(val title: String, val content: String, val isPublic: Boolean = false, val tags: String? = null)

// 待办
data class TodoDto(val id: Long, val title: String, val completed: Boolean, val priority: String, val dueDate: String?, val sourceType: String)
data class TodoRequest(val title: String, val description: String? = null, val priority: String = "medium", val sourceType: String = "manual")

// AI
data class AiChatRequest(val message: String, val conversationId: Long? = null)
data class AiChatResponse(val conversationId: Long, val messageId: Long, val content: String)
data class AiMarkdownGenerateRequest(val requirement: String)
data class AiMarkdownGenerateResponse(val content: String, val suggestedTitle: String)
data class TodoSuggestion(val title: String, val priority: String)
```

## Web API 调用

```js
// document
listDocuments()
getDocument(id)
createDocument({ title, content, isPublic, tags })
updateDocument(id, { title, content, isPublic, tags })
deleteDocument(id)

// todo
listTodos(completed?)
createTodo({ title, priority, sourceType })
batchCreateTodos([{ title, priority }])
completeTodo(id)
uncompleteTodo(id)
deleteTodo(id)

// ai
chatAi({ message, conversationId })
generateMarkdown({ requirement })
summarizeDocument(id)
extractTodos({ content })
```

---

# 10. 当前结论

MKM V1 API 规范完成。

共 **27 个接口**，覆盖：

```text
Auth × 2
Document × 6
Todo × 7
AI Provider × 6
AI 功能 × 4
User × 1 (可选)
```

这套 API 设计可以同时支撑 Android 和 Web 前端，也为后续 CLI 调用预留了清晰的 REST 接口。
