# MKM AI 功能流程设计

## 1. 文档目标

本文档定义 MKM V1 所有 AI 功能的端到端流程，包括：

- 用户操作路径
- Android 前端处理逻辑
- 后端 API 与业务逻辑
- Prompt 组装方式
- 返回结果处理

---

# 2. 普通 AI 提问

## 2.1 用户操作

```text
点击 AI Tab
→ 输入问题
→ 点击发送
→ 看到 loading
→ AI 返回结构化 Markdown 回答
→ 可以复制/保存为文档/提取待办
```

## 2.2 Android 处理

```text
AiAssistantFragment
→ 检查是否已配置默认 Provider
→ 未配置则显示引导
→ 已配置则正常发送
→ 请求 POST /api/ai/chat
→ 展示 loading 消息
→ 成功后显示 AI 回答
→ 支持长按复制/操作菜单
```

## 2.3 后端处理

```text
POST /api/ai/chat
Request:
{
  "message": "Spring Security JWT 鉴权流程",
  "conversationId": null  // 新建对话
}

→ JWT 获取当前用户
→ 获取用户默认 Provider 配置
→ 解密 API Key
→ 组装 system prompt + user message
→ 调用 OpenAI-compatible API
→ 可选保存对话和消息
→ 可选写入 ai_call_logs

Response:
{
  "conversationId": 1,
  "messageId": 2,
  "content": "## JWT 鉴权流程\\n\\n...",
  "role": "assistant"
}
```

## 2.4 错误处理

| 场景 | 处理 |
|---|---|
| 未配置 Provider | 返回 400 + 提示去配置 |
| API Key 无效 | 返回 400 + 提示 Key 失效 |
| AI 服务超时 | 返回 504 + 提示稍后重试 |
| 网络异常 | Android 本地提示 |

---

# 3. Markdown 生成

## 3.1 用户操作

```text
AI Tab 快捷按钮"生成 Markdown"
或 输入"帮我生成 xxx 学习笔记"
→ 发送
→ AI 返回完整 Markdown 文档
→ 可以复制
→ 可以一键保存为新文档
→ 可以跳转编辑
```

## 3.2 Android 处理

```text
AiAssistantFragment
→ 检测用户意图 / 快捷按钮
→ 请求 POST /api/ai/markdown/generate
→ 展示 loading
→ 展示生成的 Markdown 文本
→ 底部操作栏：复制 / 保存为文档 / 编辑
→ 点击保存后调用 POST /api/documents 创建文档
→ 成功后跳转 DocumentDetailActivity 查看效果
```

## 3.3 后端处理

```text
POST /api/ai/markdown/generate
Request:
{
  "requirement": "Kotlin 协程学习笔记"
}

→ 组装 MARKDOWN_GENERATE Prompt
→ 调用 AI Provider
→ 返回 Markdown 正文

Response:
{
  "content": "# Kotlin 协程学习笔记\\n\\n## 1. 背景\\n\\n...",
  "suggestedTitle": "Kotlin 协程学习笔记"
}
```

说明：

```text
suggestedTitle 可以从 AI 生成的 Markdown 一级标题中提取。
如果 AI 没有生成一级标题，则使用用户输入截断。
```

---

# 4. 文档总结

## 4.1 用户操作

```text
进入 Markdown 文档详情页
→ 点击"AI 总结"按钮
→ 看到 loading
→ 结果弹窗或内嵌展示
→ 可以复制
→ 可以保存为新文档
→ 可以提取待办
```

## 4.2 Android 处理

```text
DocumentDetailActivity
→ 用户点击 AI 总结
→ 请求 POST /api/ai/documents/{id}/summarize
→ 展示 loading
→ 结果以弹窗或侧拉展示总结内容
→ 提供操作按钮：复制 / 保存为新文档 / 提取待办
```

## 4.3 后端处理

```text
POST /api/ai/documents/{id}/summarize
→ JWT 获取当前用户
→ 获取文档
→ 校验权限（自己的文档或公开文档）
→ 获取用户默认 Provider
→ 组装 DOCUMENT_SUMMARIZE Prompt
→ documentContent 替换占位符
→ 如文档过长则截断并加提示
→ 调用 AI Provider

Response:
{
  "content": "## 核心摘要\\n\\n...",
  "extractedTodos": [
    "学习 Spring Security 鉴权流程",
    "完成 JWT 工具类封装"
  ]
}
```

说明：

```text
extractedTodos 是可选字段。
后端在总结时可以同时调用 TODO_EXTRACT_TEXT Prompt 提取。
也可以让前端用户单独点击"提取待办"。
```

---

# 5. 待办提取

## 5.1 触发场景

| 触发场景 | 来源 |
|---|---|
| 文档详情页点击"提取待办" | 当前文档内容 |
| AI 总结结果中点击"提取待办" | 总结文本 |
| AI 回答中点击"提取待办" | AI 回答文本 |
| 用户直接输入"给我提取待办" | 用户输入或上下文 |

## 5.2 用户操作

```text
触发提取待办
→ 展示 loading
→ AI 返回待办列表
→ 用户查看草稿列表
→ 勾选要保存的待办
→ 点击保存
→ 写入 todos 表
→ 跳转待办页或提示"已添加 N 条待办"
```

## 5.3 Android 处理

```text
→ 根据来源传入文本
→ 请求 POST /api/ai/text/todos
→ 展示 loading
→ 返回待办草稿
→ 可选：全选 / 部分选择
→ 点击保存后调用 POST /api/todos 批量写入
```

## 5.4 后端处理

```text
POST /api/ai/text/todos
Request:
{
  "content": "这周需要学 Spring Security、完成登录页、整理 JWT 笔记"
}

→ 组装 TODO_EXTRACT_TEXT Prompt
→ 调用 AI Provider
→ 返回 Markdown 格式待办列表

Response:
{
  "content": "## 待办建议\\n\\n- [ ] 学习 Spring Security\\n- [ ] 完成 Android 登录页\\n- [ ] 整理 JWT 笔记",
  "todos": [
    { "title": "学习 Spring Security", "priority": "medium" },
    { "title": "完成 Android 登录页", "priority": "high" },
    { "title": "整理 JWT 笔记", "priority": "low" }
  ]
}
```

说明：

```text
todos 字段是辅助字段，用于前端直接映射创建 TodoRequest。
content 是供展示用的 Markdown 文本。
```

---

# 6. AI 回答保存为 Markdown

## 6.1 用户操作

```text
AI 回答后
→ 点击"保存为文档"
→ 后端整理格式
→ 创建新文档
→ 跳转文档详情页
```

## 6.2 Android 处理

```text
操作菜单"保存为文档"
→ 可选：先整理格式 / 直接保存
→ 如需整理格式：POST /api/ai/markdown/format
→ 创建文档：POST /api/documents
→ 跳转 DocumentDetailActivity
```

## 6.3 后端处理

```text
V1 简化方案：
直接将 AI 回答作为 content 保存为文档。

V2 优化方案：
POST /api/ai/markdown/format
Request:
{
  "content": "AI 原始回答文本"
}
→ 调用 ANSWER_TO_MARKDOWN Prompt
→ 返回整理后的 Markdown 正文
→ 前端再调用 POST /api/documents 保存
```

---

# 7. 完整 API 接口清单

| 方法 | 路径 | 说明 | 所需权限 |
|---|---|---|---|
| POST | /api/ai/chat | 普通提问 | 已登录 + Provider 已配置 |
| POST | /api/ai/markdown/generate | 生成 Markdown | 已登录 + Provider 已配置 |
| POST | /api/ai/documents/{id}/summarize | 总结文档 | 已登录 + 有文档访问权限 |
| POST | /api/ai/text/todos | 提取待办 | 已登录 + Provider 已配置 |
| POST | /api/ai/markdown/format | 整理 AI 回答，V2 | 已登录 + Provider 已配置 |
| GET | /api/ai/providers | 获取 Provider 列表 | 已登录 |
| POST | /api/ai/providers | 添加 Provider | 已登录 |
| PUT | /api/ai/providers/{id} | 更新 Provider | 已登录 + 自己的配置 |
| DELETE | /api/ai/providers/{id} | 删除 Provider | 已登录 + 自己的配置 |
| PUT | /api/ai/providers/{id}/default | 设为默认 | 已登录 + 自己的配置 |
| POST | /api/ai/providers/{id}/test | 测试 Provider | 已登录 |

---

# 8. AI 功能与数据流

## 8.1 总结文档数据流

```text
Android DocumentDetailActivity
→ POST /api/ai/documents/{id}/summarize
→ Backend: 取文档 → 解密 Key → 组装 Prompt → 调用模型
→ Response: { content, todos }
→ Android: 显示总结 → 可提取待办
→ 用户确认 → POST /api/todos（批量）
→ todos 写入数据库
→ Android: 跳转或提示成功
```

## 8.2 生成 Markdown 数据流

```text
Android AiAssistantFragment
→ POST /api/ai/markdown/generate
→ Backend: 组装 Prompt → 调用模型
→ Response: { content, suggestedTitle }
→ Android: 显示 Markdown
→ 用户点击保存
→ POST /api/documents { title, content }
→ documents 写入数据库
→ Android: 跳转 DocumentDetailActivity
```

## 8.3 待办提取数据流

```text
Android（任意来源）
→ POST /api/ai/text/todos
→ Backend: 组装 Prompt → 调用模型 → 解析结果
→ Response: { content, todos[] }
→ Android: 展示待办草稿 → 用户勾选
→ POST /api/todos（batch）
→ 写入 todos 表
→ Android: 提示成功，可去待办页查看
```

---

# 9. 无 Provider 配置时

## 9.1 Android 处理

```text
进入 AI Tab
→ 检查 GET /api/ai/providers
→ 返回空列表
→ 显示空状态

空状态内容：
"你还没有配置 AI 模型
 请前往 我的 → AI 设置 添加 DeepSeek、MiniMax 等模型"

按钮：去配置
```

## 9.2 后端处理

```text
所有 AI 功能接口，先检查当前用户是否有默认 Provider。
如无，返回：

HTTP 400
{
  "error": "NO_PROVIDER_CONFIGURED",
  "message": "请先在 AI 设置中配置模型"
}
```

---

# 10. V1 推荐实现顺序

```text
1. ai_provider_configs 数据模型和 API Key 加密
2. Provider CRUD 接口
3. Provider 测试接口
4. OpenAI-compatible Provider Adapter
5. POST /api/ai/chat 接口
6. Android AI 基础聊天页面
7. POST /api/ai/markdown/generate
8. Android Markdown 生成 + 保存为文档
9. POST /api/ai/documents/{id}/summarize
10. Android 文档总结
11. POST /api/ai/text/todos
12. Android 待办提取 + 写入 todos
```

---

# 11. 当前结论

MKM AI V1 功能流程设计完成。

四条核心 AI 链路：

```text
普通提问 → AI 回答 → 可复制 / 保存为文档
生成 Markdown → 查看 → 保存为文档 → 跳转文档详情
文档总结 → 查看摘要 → 可提取待办
待办提取 → 查看草稿 → 勾选确认 → 写入 todos
```

每条链路的起点和终点：

| 链路 | 起点 | 终点 |
|---|---|---|
| 普通提问 | AI Tab 输入 | AI 回答展示 |
| Markdown 生成 | AI Tab / 快捷按钮 | documents 表 + 文档详情页 |
| 文档总结 | 文档详情页 | AI 摘要展示 + 可选保存 |
| 待办提取 | 文档/AI 回答/用户输入 | todos 表 + 待办页 |
