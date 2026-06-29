# MKM 用户流程文档

## 1. 文档目标

本文档用于描述 MKM V1 的核心用户流程，每个流程同时包含：

1. **用户操作路径**：用户在 Android / Web 界面上如何操作。
2. **系统处理逻辑**：前端、后端、数据库和 AI 服务分别做什么。

V1 采用 Android 优先策略，因此流程以 Android 为主，Web 作为辅助管理端。

---

## 2. 全局页面结构

V1 登录后进入主界面，底部导航为：

```text
Markdown    待办    AI    知识库    我的
```

### 全局状态

| 状态 | 处理方式 |
|---|---|
| 未登录 | 跳转登录页 |
| 已登录 | 进入主界面 |
| Token 过期 | 清除本地登录状态，回到登录页 |
| 网络异常 | 优先显示本地缓存，并提示同步失败 |
| 后端异常 | 显示错误提示，不清空本地数据 |

---

# 3. 登录 / 注册流程

## 3.1 用户操作路径

```text
打开 App
→ 进入登录页
→ 输入用户名和密码
→ 点击登录 / 注册
→ 成功后进入 Markdown 首页
```

## 3.2 Android 处理逻辑

```text
LoginActivity
→ 校验用户名 / 密码非空
→ 调用 Auth API
→ 成功后保存 JWT Token
→ 跳转 MainActivity
```

## 3.3 后端处理逻辑

### 注册

```text
POST /api/auth/register
→ 校验用户名是否存在
→ 密码 BCrypt 加密
→ 写入 users 表
→ 生成 JWT
→ 返回 token + username
```

### 登录

```text
POST /api/auth/login
→ 校验用户名和密码
→ 生成 JWT
→ 返回 token + username
```

## 3.4 数据库读写

| 操作 | 表 |
|---|---|
| 查询用户名是否存在 | users |
| 创建用户 | users |
| 登录校验 | users |

## 3.5 异常状态

| 场景 | 用户提示 |
|---|---|
| 用户名为空 | 请输入用户名 |
| 密码为空 | 请输入密码 |
| 用户名已存在 | 用户名已存在 |
| 密码错误 | 用户名或密码错误 |
| 网络错误 | 网络异常，请稍后重试 |

---

# 4. Markdown 文档列表流程

## 4.1 用户操作路径

```text
进入 App
→ 默认打开 Markdown Tab
→ 查看自己的文档列表
→ 下拉刷新 / 自动同步
→ 点击文档进入详情
→ 点击新建创建文档
```

## 4.2 Android 处理逻辑

```text
DocumentListFragment
→ 先读取 Room 本地缓存
→ 同时请求后端 /api/documents
→ 成功后更新 Room
→ RecyclerView 展示文档列表
```

## 4.3 后端处理逻辑

```text
GET /api/documents
→ JWT 解析当前用户
→ 查询该用户 documents
→ 按 updatedAt 倒序返回
```

## 4.4 数据库读写

| 操作 | 表 |
|---|---|
| 查询用户文档列表 | documents |
| Android 缓存文档列表 | Room documents |

## 4.5 异常状态

| 场景 | 处理 |
|---|---|
| 网络正常 | 展示最新后端数据 |
| 网络失败但有缓存 | 展示缓存 + 提示同步失败 |
| 网络失败且无缓存 | 展示空态 + 重试按钮 |
| Token 过期 | 跳转登录页 |

---

# 5. Markdown 新建 / 编辑流程

## 5.1 用户操作路径

### 新建文档

```text
Markdown Tab
→ 点击 + 按钮
→ 输入标题
→ 输入 Markdown 内容
→ 点击保存
→ 回到文档列表 / 进入文档详情
```

### 编辑文档

```text
文档详情页
→ 点击编辑
→ 修改标题或内容
→ 点击保存
→ 回到预览模式
```

## 5.2 Android 处理逻辑

```text
DocumentDetailActivity
→ 新建模式：docId = -1
→ 编辑模式：已有 docId
→ 用户点击保存
→ 调用 createDocument / updateDocument API
→ 成功后刷新本地缓存
```

## 5.3 后端处理逻辑

### 新建

```text
POST /api/documents
→ JWT 获取当前用户
→ 校验 title 非空
→ 写入 documents 表
→ 返回 DocumentDto
```

### 编辑

```text
PUT /api/documents/{id}
→ JWT 获取当前用户
→ 查询文档
→ 校验 owner 是否为当前用户
→ 更新 title/content/isPublic/updatedAt
→ 返回 DocumentDto
```

## 5.4 数据库读写

| 操作 | 表 |
|---|---|
| 新建文档 | documents |
| 更新文档 | documents |
| 更新本地缓存 | Room documents |

## 5.5 异常状态

| 场景 | 用户提示 |
|---|---|
| 标题为空 | 请输入标题 |
| 保存成功 | 保存成功 |
| 保存失败 | 保存失败，请重试 |
| 无权限编辑 | 无权编辑该文档 |
| 网络失败 | 网络异常，暂未保存到云端 |

---

# 6. Markdown 查看流程

## 6.1 用户操作路径

```text
Markdown 列表
→ 点击一篇文档
→ 进入文档详情
→ 查看渲染后的 Markdown
→ 可切换编辑
→ 可点击 AI 总结
→ 可生成待办
```

## 6.2 Android 处理逻辑

```text
DocumentDetailActivity
→ 根据 docId 请求后端文档详情
→ 若失败则尝试读取本地缓存
→ 使用 Markwon 渲染 content
→ 展示标题、内容、更新时间
```

## 6.3 后端处理逻辑

```text
GET /api/documents/{id}
→ 判断文档是否属于当前用户或是否公开
→ 有权限则返回文档详情
→ 无权限则返回 403
```

## 6.4 Markdown 渲染要求

V1 至少支持：

- 标题
- 段落
- 粗体 / 斜体
- 列表
- 任务列表
- 引用
- 代码块
- 表格
- 链接
- 图片基础展示

---

# 7. 待办流程

## 7.1 用户操作路径

```text
点击待办 Tab
→ 查看待办列表
→ 点击新建待办
→ 输入任务内容
→ 保存
→ 点击完成 / 取消完成
→ 删除待办
```

## 7.2 Android 处理逻辑

```text
TodoFragment
→ 请求 /api/todos
→ 展示未完成和已完成任务
→ 新建时调用 POST /api/todos
→ 完成时调用 PUT /api/todos/{id}
→ 删除时调用 DELETE /api/todos/{id}
```

## 7.3 后端处理逻辑

```text
TodoController
→ JWT 获取当前用户
→ 只允许访问自己的 todos
→ 新建 / 查询 / 更新 / 删除 todos
```

## 7.4 数据库读写

V1 新增表：`todos`

建议字段：

| 字段 | 说明 |
|---|---|
| id | 主键 |
| title | 待办标题 |
| description | 描述，可选 |
| completed | 是否完成 |
| priority | 优先级，可选 |
| dueDate | 截止时间，可选 |
| documentId | 关联文档，可选 |
| ownerId | 所属用户 |
| createdAt | 创建时间 |
| updatedAt | 更新时间 |

---

# 8. AI 普通提问流程

## 8.1 用户操作路径

```text
点击 AI Tab
→ 输入问题
→ 点击发送
→ 等待 AI 回复
→ 查看回答
→ 复制回答 / 保存为文档 / 创建待办（可选）
```

## 8.2 Android 处理逻辑

```text
AiAssistantFragment
→ 用户输入 message
→ 调用 POST /api/ai/chat
→ 展示 loading
→ 返回后显示 assistant message
```

## 8.3 后端处理逻辑

```text
POST /api/ai/chat
→ JWT 获取当前用户
→ 组装 Prompt
→ 调用 AI Provider
→ 返回回答文本
→ 可选：保存对话记录
```

## 8.4 数据库读写

V1 可选保存 AI 对话：

| 表 | 说明 |
|---|---|
| ai_conversations | 对话会话 |
| ai_messages | 单条消息 |

如果 V1 想降低复杂度，可以先不保存对话，只返回结果。

## 8.5 异常状态

| 场景 | 用户提示 |
|---|---|
| AI 服务不可用 | AI 服务暂时不可用 |
| 超时 | 请求超时，请稍后重试 |
| 输入为空 | 请输入问题 |
| Token 过期 | 请重新登录 |

---

# 9. 当前文档 AI 总结流程

## 9.1 用户操作路径

```text
进入 Markdown 文档详情
→ 点击 AI 总结
→ 系统发送当前文档内容
→ AI 返回摘要
→ 用户复制 / 保存为新文档 / 创建待办
```

## 9.2 Android 处理逻辑

```text
DocumentDetailActivity
→ 用户点击总结按钮
→ 获取当前 docId
→ 调用 POST /api/ai/documents/{id}/summarize
→ 展示总结结果弹窗或跳转 AI 页面
```

## 9.3 后端处理逻辑

```text
POST /api/ai/documents/{id}/summarize
→ JWT 获取当前用户
→ 查询文档
→ 校验权限
→ 构造总结 Prompt
→ 调用 AI Provider
→ 返回摘要
```

## 9.4 Prompt 目标

AI 总结应默认输出：

```text
1. 核心内容摘要
2. 关键知识点
3. 可以执行的待办
4. 推荐复习点
```

---

# 10. AI 生成待办流程

## 10.1 用户操作路径

```text
文档详情 / AI 回答
→ 点击生成待办
→ AI 提取任务草稿
→ 用户确认
→ 写入待办列表
```

## 10.2 系统处理逻辑

```text
前端发送文档内容或 AI 回答文本
→ 后端调用 AI 提取待办
→ 返回待办数组
→ 用户选择要保存的待办
→ 前端调用 POST /api/todos 批量创建
```

## 10.3 V1 简化方案

V1 可以先做半自动：

```text
AI 返回“建议待办列表”文本
→ 用户手动复制 / 点击某一条创建待办
```

不必一开始实现复杂的结构化 JSON 解析。

---

# 11. 更多知识库流程

## 11.1 用户操作路径

```text
点击知识库 Tab
→ 查看示例知识库 / 公开知识库
→ 点击某个知识库
→ 查看文档列表
→ 打开公开 Markdown 文档
```

## 11.2 Android 处理逻辑

```text
KnowledgeBaseFragment
→ 请求 GET /api/documents/public
→ 展示公开文档列表
→ 点击后复用 DocumentDetailActivity 只读模式
```

## 11.3 后端处理逻辑

```text
GET /api/documents/public
→ 查询 isPublic = true 的 documents
→ 返回公开文档列表
```

## 11.4 V1 简化方案

V1 可以先不做完整 knowledge_bases 表，而是直接复用公开文档：

```text
更多知识库 = 公开文档列表 + 示例文档
```

V3 再引入真正的知识库集合、用户公开主页和评论。

---

# 12. 我的流程

## 12.1 用户操作路径

```text
点击我的 Tab
→ 查看用户名
→ 查看同步状态
→ 进入设置
→ 退出登录
```

## 12.2 Android 处理逻辑

```text
ProfileFragment
→ 读取本地 token / username
→ 展示当前用户
→ 点击退出
→ 清除 token
→ 跳转 LoginActivity
```

## 12.3 后端处理逻辑

V1 可选提供：

```text
GET /api/users/me
→ 返回当前用户信息
```

如果不做该接口，Android 可以先展示本地保存的 username。

---

# 13. Web 辅助管理流程

## 13.1 Web 登录流程

```text
访问 Web
→ 未登录跳转 /login
→ 登录成功保存 token
→ 跳转文档列表
```

## 13.2 Web 文档流程

```text
文档列表
→ 新建文档
→ 编辑 Markdown
→ 保存到后端
→ Android 刷新后同步看到
```

## 13.3 Web V1 定位

Web 端 V1 不承担复杂交互，主要用于：

- 快速输入较长 Markdown
- 管理文档
- 验证后端 API
- 支撑多端同步概念

---

# 14. 推荐开发顺序

为了减少返工，推荐按以下顺序开发 V1：

```text
1. Auth 登录注册
2. Markdown 文档 CRUD
3. Android Markdown 渲染
4. Web 文档管理
5. Todo 待办 CRUD
6. AI 普通提问
7. 当前文档 AI 总结
8. 更多知识库公开文档
9. 我的设置完善
```

---

# 15. 当前结论

MKM V1 的核心用户流程以 Android 为主，后端负责统一数据和 AI 能力，Web 作为辅助管理端。

最重要的三条产品闭环是：

```text
Markdown 闭环：创建 → 保存 → 渲染 → 编辑
AI 闭环：提问 → 回答 → 保存/复制/转待办
同步闭环：Web 创建 → 后端保存 → Android 查看
```

只要这三条闭环跑通，MKM 就已经具备从学习项目走向真实产品雏形的基础。
