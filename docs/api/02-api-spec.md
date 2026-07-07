# MKM API 接口规范

## 1. 文档目标

本文档定义 MKM V1 所有 REST API 的完整规范，包括：

- 公共约定
- Auth 接口
- User 接口
- Document 接口
- Attachment 接口
- Todo 接口
- AI 接口
- 请求/响应字段说明

V1 不包含社区、评论、点赞、公开知识库接口。

---

## 2. 公共约定

## 2.1 Base URL

```text
http://localhost:8080/api
```

生产环境通过 Nginx 或云服务代理。

---

## 2.2 认证

除注册和登录外，所有接口需要 JWT Bearer Token：

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
  "title": "Spring Security 学习笔记"
}
```

### 成功 - 数组

```json
[
  { "id": 1, "title": "A" },
  { "id": 2, "title": "B" }
]
```

### 错误

```json
{
  "error": "VALIDATION_ERROR",
  "message": "title must not be blank"
}
```

---

## 2.5 状态码

| 状态码 | 含义 |
|---|---|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 参数错误 |
| 401 | 未登录 / Token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 冲突 |
| 413 | 附件超过大小限制 |
| 500 | 服务端错误 |

---

# 3. Auth API

## 3.1 注册

```http
POST /api/auth/register
```

请求：

```json
{
  "username": "windmeta",
  "password": "123456",
  "email": "windmeta@example.com"
}
```

响应：

```json
{
  "token": "jwt-token",
  "user": {
    "id": 1,
    "username": "windmeta",
    "nickname": null,
    "email": "windmeta@example.com",
    "avatarUrl": null
  }
}
```

---

## 3.2 登录

```http
POST /api/auth/login
```

请求：

```json
{
  "username": "windmeta",
  "password": "123456"
}
```

响应同注册。

---

# 4. User API

## 4.1 获取当前用户

```http
GET /api/users/me
```

响应：

```json
{
  "id": 1,
  "username": "windmeta",
  "nickname": "Wind",
  "email": "windmeta@example.com",
  "avatarUrl": "https://example.com/avatar.png",
  "createdAt": "2024-11-20T14:30:00"
}
```

---

## 4.2 更新当前用户

```http
PATCH /api/users/me
```

请求：

```json
{
  "nickname": "Wind",
  "avatarUrl": "https://example.com/avatar.png"
}
```

响应：当前用户对象。

---

# 5. Document API

## 5.1 获取文档列表

```http
GET /api/documents?q=&tag=&folder=&page=0&pageSize=20
```

查询参数：

| 参数 | 说明 |
|---|---|
| q | 搜索文件名、标签、正文内容 |
| tag | 标签筛选 |
| folder | 文件夹路径，如 `/Android/` |
| page | 页码，从 0 开始 |
| pageSize | 每页数量 |

响应：

```json
{
  "items": [
    {
      "id": 1,
      "title": "Room 学习笔记",
      "fileName": "Room.md",
      "folderPath": "/Android/",
      "tags": ["android", "room"],
      "syncEnabled": true,
      "createdAt": "2024-11-20T14:30:00",
      "updatedAt": "2024-11-20T14:30:00"
    }
  ],
  "page": 0,
  "pageSize": 20,
  "total": 1
}
```

---

## 5.2 获取文件夹树

```http
GET /api/documents/tree
```

响应：

```json
{
  "name": "root",
  "path": "/",
  "type": "folder",
  "children": [
    {
      "name": "Android",
      "path": "/Android/",
      "type": "folder",
      "children": [
        {
          "id": 1,
          "name": "Room.md",
          "path": "/Android/Room.md",
          "type": "document"
        }
      ]
    }
  ]
}
```

---

## 5.3 创建文档

```http
POST /api/documents
```

请求：

```json
{
  "title": "Room 学习笔记",
  "fileName": "Room.md",
  "folderPath": "/Android/",
  "content": "# Room\n\nRoom 是 Jetpack 数据库组件。",
  "tags": ["android", "room"],
  "syncEnabled": true,
  "localUpdatedAt": "2024-11-20T14:30:00"
}
```

响应：`201 Created`，返回文档详情。

---

## 5.4 获取文档详情

```http
GET /api/documents/{id}
```

响应：

```json
{
  "id": 1,
  "title": "Room 学习笔记",
  "fileName": "Room.md",
  "folderPath": "/Android/",
  "content": "# Room\n\nRoom 是 Jetpack 数据库组件。",
  "tags": ["android", "room"],
  "syncEnabled": true,
  "localUpdatedAt": "2024-11-20T14:30:00",
  "createdAt": "2024-11-20T14:30:00",
  "updatedAt": "2024-11-20T14:30:00"
}
```

---

## 5.5 更新文档

```http
PUT /api/documents/{id}
```

请求：

```json
{
  "title": "Room 学习笔记 - 更新",
  "fileName": "Room.md",
  "folderPath": "/Android/",
  "content": "# Room\n\n更新后的内容。",
  "tags": ["android", "room"],
  "syncEnabled": true,
  "localUpdatedAt": "2024-11-20T15:00:00"
}
```

冲突响应：`409 Conflict`

```json
{
  "error": "SYNC_CONFLICT",
  "message": "Document has remote changes",
  "remoteDocument": {
    "id": 1,
    "title": "云端版本",
    "content": "云端内容",
    "updatedAt": "2024-11-20T15:01:00"
  }
}
```

客户端收到 409 后弹窗让用户选择保留本地或保留云端。

---

## 5.6 删除文档

```http
DELETE /api/documents/{id}
```

响应：`204 No Content`

---

# 6. Attachment API

## 6.1 上传附件

```http
POST /api/documents/{id}/attachments
Content-Type: multipart/form-data
```

字段：

| 字段 | 说明 |
|---|---|
| file | 附件文件，最大 10MB |
| relativePath | 相对知识库根目录路径，如 `./images/a.png` |

响应：

```json
{
  "id": 10,
  "documentId": 1,
  "fileName": "a.png",
  "relativePath": "./images/a.png",
  "mimeType": "image/png",
  "fileSize": 20480,
  "downloadUrl": "/api/attachments/10"
}
```

超过 10MB 返回 `413 Payload Too Large`。

---

## 6.2 获取文档附件列表

```http
GET /api/documents/{id}/attachments
```

响应：附件数组。

---

## 6.3 下载附件

```http
GET /api/attachments/{id}
```

响应：二进制文件流。

---

## 6.4 根据相对路径解析附件

```http
GET /api/documents/{id}/attachments/resolve?path=./images/a.png
```

响应：

```json
{
  "id": 10,
  "relativePath": "./images/a.png",
  "downloadUrl": "/api/attachments/10"
}
```

---

## 6.5 删除附件

```http
DELETE /api/attachments/{id}
```

响应：`204 No Content`

---

# 7. Todo API

## 7.1 获取 Todo 列表

```http
GET /api/todos?completed=&from=&to=
```

响应：

```json
[
  {
    "id": 1,
    "title": "整理 Room 笔记",
    "note": "补充 DAO 示例",
    "priority": "MEDIUM",
    "dueDate": "2024-11-21T18:00:00",
    "completed": false,
    "sourceType": "manual",
    "sourceDocumentId": 1,
    "sourceDocumentTitle": "Room 学习笔记",
    "createdAt": "2024-11-20T14:30:00",
    "updatedAt": "2024-11-20T14:30:00"
  }
]
```

---

## 7.2 创建 Todo

```http
POST /api/todos
```

请求：

```json
{
  "title": "整理 Room 笔记",
  "note": "补充 DAO 示例",
  "priority": "MEDIUM",
  "dueDate": "2024-11-21T18:00:00",
  "sourceDocumentId": 1
}
```

响应：`201 Created`，返回 Todo。

---

## 7.3 更新 Todo

```http
PUT /api/todos/{id}
```

---

## 7.4 删除 Todo

```http
DELETE /api/todos/{id}
```

---

## 7.5 完成 / 取消完成

```http
PUT /api/todos/{id}/complete
PUT /api/todos/{id}/uncomplete
```

---

## 7.6 批量创建 Todo

```http
POST /api/todos/batch
```

用于 AI 提取待办。

---

# 8. AI Provider API

## 8.1 获取 Provider 配置

```http
GET /api/ai/providers
```

响应：

```json
{
  "id": 1,
  "providerName": "DeepSeek",
  "baseUrl": "https://api.deepseek.com/v1",
  "maskedApiKey": "sk-abc****789",
  "modelName": "deepseek-chat",
  "enabled": true
}
```

V1 每个用户只支持一个 OpenAI 兼容 Provider。

---

## 8.2 保存 Provider 配置

```http
POST /api/ai/providers
```

请求：

```json
{
  "providerName": "DeepSeek",
  "baseUrl": "https://api.deepseek.com/v1",
  "apiKey": "sk-xxxx",
  "modelName": "deepseek-chat"
}
```

响应：脱敏后的 Provider 配置。

---

## 8.3 更新 Provider 配置

```http
PUT /api/ai/providers/{id}
```

---

## 8.4 删除 Provider 配置

```http
DELETE /api/ai/providers/{id}
```

---

## 8.5 测试 Provider 连接

```http
POST /api/ai/providers/{id}/test
```

响应：

```json
{
  "success": true,
  "message": "Connection ok"
}
```

---

# 9. AI Conversation API

## 9.1 获取会话列表

```http
GET /api/ai/conversations
```

响应：

```json
[
  {
    "id": 1,
    "title": "Room 文档总结",
    "sourceType": "document_summary",
    "sourceDocumentId": 1,
    "createdAt": "2024-11-20T14:30:00",
    "updatedAt": "2024-11-20T14:30:00"
  }
]
```

---

## 9.2 新建会话

```http
POST /api/ai/conversations
```

请求：

```json
{
  "title": "新对话",
  "sourceType": "chat",
  "sourceDocumentId": null
}
```

---

## 9.3 获取会话消息

```http
GET /api/ai/conversations/{id}/messages
```

响应：

```json
[
  {
    "id": 1,
    "role": "user",
    "content": "帮我总结这篇文档",
    "createdAt": "2024-11-20T14:30:00"
  },
  {
    "id": 2,
    "role": "assistant",
    "content": "这篇文档主要介绍了...",
    "createdAt": "2024-11-20T14:30:05"
  }
]
```

---

## 9.4 删除会话

```http
DELETE /api/ai/conversations/{id}
```

---

# 10. AI Function API

## 10.1 普通提问

```http
POST /api/ai/chat
```

请求：

```json
{
  "conversationId": 1,
  "message": "解释一下 Room 和 SQLite 的关系"
}
```

响应：

```json
{
  "conversationId": 1,
  "message": {
    "id": 20,
    "role": "assistant",
    "content": "Room 是 SQLite 之上的抽象层...",
    "createdAt": "2024-11-20T14:30:00"
  }
}
```

---

## 10.2 生成 Markdown

```http
POST /api/ai/markdown/generate
```

请求：

```json
{
  "conversationId": 1,
  "topic": "Spring Security JWT 登录流程",
  "style": "technical-note"
}
```

---

## 10.3 文档总结

```http
POST /api/ai/document/summarize
```

请求：

```json
{
  "documentId": 1,
  "conversationId": 1
}
```

---

## 10.4 提取待办

```http
POST /api/ai/todo/extract
```

请求：

```json
{
  "documentId": 1,
  "text": "明天补充 DAO 示例，并整理 Room 迁移笔记。"
}
```

响应：

```json
{
  "items": [
    {
      "title": "补充 DAO 示例",
      "note": null,
      "priority": "MEDIUM",
      "dueDate": null
    }
  ]
}
```

客户端确认后调用 `/api/todos/batch` 保存。

---

# 11. 安全要求

## 11.1 API Key 安全

```text
数据库不保存明文 API Key
使用 AI_KEY_ENCRYPTION_SECRET 对称加密
返回前端只返回 maskedApiKey
日志不打印 API Key
错误信息不暴露 API Key
用户可以删除配置
```

---

## 11.2 AI 日志安全

ai_call_logs 只保存：

```text
userId
providerId
feature
status
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

## 11.3 附件安全

```text
附件下载必须鉴权
必须校验 owner
单文件限制 10MB
禁止上传路径穿越文件名
```

---

# 12. V1 接口验收清单

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/users/me
PATCH /api/users/me
GET  /api/documents
GET  /api/documents/tree
POST /api/documents
GET  /api/documents/{id}
PUT  /api/documents/{id}
DELETE /api/documents/{id}
POST /api/documents/{id}/attachments
GET  /api/documents/{id}/attachments
GET  /api/documents/{id}/attachments/resolve
GET  /api/attachments/{id}
DELETE /api/attachments/{id}
GET  /api/todos
POST /api/todos
PUT  /api/todos/{id}
DELETE /api/todos/{id}
PUT  /api/todos/{id}/complete
PUT  /api/todos/{id}/uncomplete
GET  /api/ai/providers
POST /api/ai/providers
PUT  /api/ai/providers/{id}
DELETE /api/ai/providers/{id}
POST /api/ai/providers/{id}/test
GET  /api/ai/conversations
POST /api/ai/conversations
GET  /api/ai/conversations/{id}/messages
DELETE /api/ai/conversations/{id}
POST /api/ai/chat
POST /api/ai/markdown/generate
POST /api/ai/document/summarize
POST /api/ai/todo/extract
```
