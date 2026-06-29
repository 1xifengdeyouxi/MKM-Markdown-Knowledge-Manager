# MKM AI 总体架构设计

## 1. 文档目标

本文档定义 MKM V1 的 AI 模块总体架构，包括：

- AI 模块定位
- V1 能力边界
- 后端架构
- 前端 / Android 调用方式
- 数据模型方向
- 后续扩展路径

MKM V1 的 AI 设计采用：

```text
可持续扩展 AI 助手
```

也就是：

- V1 不做复杂 RAG
- V1 不做 Agent 自动执行复杂任务
- V1 先跑通 AI 提问、文档总结、Markdown 生成、待办生成
- 同时预留 Provider 配置、对话历史、调用日志，为后续知识库问答和 Agent 集成打基础

---

## 2. AI 模块定位

AI 模块不是独立聊天机器人，而是 MKM 知识库的增强层。

它的核心作用是：

```text
帮助用户围绕 Markdown 文档进行理解、生成、整理和行动沉淀。
```

V1 中 AI 主要服务四类场景：

| 场景 | 说明 |
|---|---|
| 普通提问 | 用户直接向 AI 提问技术问题或学习问题 |
| 文档总结 | AI 总结当前 Markdown 文档 |
| Markdown 生成 | 根据用户需求生成 Markdown 文档草稿 |
| 待办生成 | 从文档内容或 AI 回答中提取待办事项 |

---

## 3. V1 AI 能力范围

## 3.1 V1 必做能力

### 1. 普通 AI 提问

用户在 AI Tab 中输入问题，AI 返回回答。

示例：

```text
用户：帮我解释一下 Spring Security 的 JWT 鉴权流程
AI：返回结构化解释
```

### 2. 当前文档总结

用户在 Markdown 文档详情页点击“AI 总结”，AI 总结当前文档。

输出建议包含：

```text
1. 核心摘要
2. 关键知识点
3. 重要代码 / 命令
4. 可转化为待办的行动项
5. 推荐复习点
```

### 3. Markdown 生成

用户输入需求，AI 生成一份 Markdown 草稿。

示例：

```text
用户：帮我生成一份 Spring Boot JWT 学习笔记
AI：输出完整 Markdown 文档
```

生成结果可以：

- 复制
- 保存为新 Markdown 文档
- 继续编辑

### 4. 待办生成

AI 从文档内容或用户输入中提取任务。

示例：

```text
输入：我这周要学习 Spring Security、完成 Android 登录页、整理 JWT 笔记
输出：
- [ ] 学习 Spring Security 基础概念
- [ ] 完成 Android 登录页
- [ ] 整理 JWT 学习笔记
```

生成结果可以：

- 作为文本展示
- 用户勾选后保存到待办模块
- 后续支持批量创建 todo

---

## 3.2 V1 可选能力

| 能力 | 说明 |
|---|---|
| 保存 AI 对话历史 | 保存用户和 AI 的消息，方便后续查看 |
| AI 回答保存为文档 | 将 AI 回答一键保存为 Markdown |
| AI 回答创建待办 | 从回答中提取任务后写入 todos 表 |
| Provider 连接测试 | 用户配置 API Key 后测试是否可用 |
| AI 调用日志 | 记录模型、耗时、是否成功，不记录敏感内容 |

---

## 3.3 V1 暂不做能力

| 暂不做 | 原因 |
|---|---|
| 全知识库 RAG | 需要切片、向量库、检索排序，复杂度较高 |
| 向量数据库 | V1 先不引入额外基础设施 |
| 多文档上下文问答 | 容易涉及上下文长度和检索质量问题 |
| Agent 自动执行 | 涉及权限、安全和工具调用，放到后期 |
| 多模型并行比较 | 非 V1 核心需求 |
| 复杂 Prompt 编排 | V1 先用固定模板 |

---

## 4. 总体架构

## 4.1 架构图

```text
Android / Web
    |
    | 1. 用户提问 / 总结文档 / 生成 Markdown / 生成待办
    v
Backend Spring Boot
    |
    | 2. 鉴权 JWT，获取当前用户
    v
AI Service
    |
    | 3. 读取用户默认 Provider 配置
    v
Provider Adapter
    |
    | 4. 解密用户 API Key
    v
Spring AI / OpenAI-compatible Client
    |
    | 5. 调用 DeepSeek / MiniMax / OpenAI 等模型
    v
AI Response
    |
    | 6. 保存对话/日志，可选
    v
Android / Web 展示结果
```

---

## 4.2 后端分层

推荐后端 AI 包结构：

```text
com.mkm.ai
├── controller
│   └── AiController
├── service
│   ├── AiService
│   ├── AiProviderConfigService
│   ├── AiConversationService
│   └── AiPromptService
├── provider
│   ├── AiProviderAdapter
│   ├── OpenAiCompatibleProviderAdapter
│   └── ProviderClientFactory
├── model
│   ├── AiProviderConfig
│   ├── AiConversation
│   ├── AiMessage
│   └── AiCallLog
├── repository
│   ├── AiProviderConfigRepository
│   ├── AiConversationRepository
│   ├── AiMessageRepository
│   └── AiCallLogRepository
└── dto
    ├── AiChatRequest
    ├── AiChatResponse
    ├── AiProviderConfigRequest
    ├── AiProviderConfigResponse
    ├── MarkdownGenerateRequest
    └── TodoExtractResponse
```

如果前期想降低代码复杂度，也可以先沿用当前项目的分层结构：

```text
controller / service / model / repository / dto
```

后续再重构到模块化包结构。

---

## 5. 核心服务职责

## 5.1 AiController

负责接收前端请求。

主要接口：

```text
POST /api/ai/chat
POST /api/ai/markdown/generate
POST /api/ai/documents/{id}/summarize
POST /api/ai/documents/{id}/todos
POST /api/ai/text/todos
```

---

## 5.2 AiService

负责 AI 业务编排。

职责：

- 获取当前用户
- 获取默认 Provider 配置
- 选择 Prompt 模板
- 调用 Provider Adapter
- 返回结果
- 可选保存对话和日志

---

## 5.3 AiProviderConfigService

负责用户模型配置。

职责：

- 添加 Provider
- 更新 Provider
- 删除 Provider
- 设置默认 Provider
- 测试连接
- 加密 / 解密 API Key
- 返回脱敏后的 Key 信息

---

## 5.4 AiPromptService

负责 Prompt 模板管理。

V1 可以先写成后端常量或简单类方法：

```text
buildChatPrompt(userInput)
buildSummarizePrompt(documentContent)
buildMarkdownGeneratePrompt(userRequirement)
buildTodoExtractPrompt(content)
```

后续可以升级为数据库可配置模板。

---

## 5.5 Provider Adapter

负责屏蔽不同模型供应商差异。

V1 优先支持：

```text
OpenAI-compatible Provider
```

因为 DeepSeek、MiniMax、通义千问、智谱等国内模型通常可以通过兼容 OpenAI 的 API 形式接入。

Provider Adapter 统一输入输出：

```text
输入：modelName + baseUrl + apiKey + messages
输出：assistantText
```

---

## 6. 数据模型概览

## 6.1 ai_provider_configs

保存用户自己的模型配置。

核心字段：

```text
id
owner_id
provider_name
provider_type
base_url
encrypted_api_key
model_name
enabled
default_provider
created_at
updated_at
```

---

## 6.2 ai_conversations

保存 AI 对话会话，可选但建议 V1 预留。

核心字段：

```text
id
owner_id
title
provider_config_id
source_type
source_document_id
created_at
updated_at
```

source_type 示例：

```text
chat
document_summary
markdown_generate
todo_extract
```

---

## 6.3 ai_messages

保存单条对话消息。

核心字段：

```text
id
conversation_id
role
content
created_at
```

role 示例：

```text
system
user
assistant
```

---

## 6.4 ai_call_logs

保存 AI 调用日志，便于排查和统计。

核心字段：

```text
id
owner_id
provider_config_id
feature_type
model_name
success
error_message
latency_ms
created_at
```

注意：

```text
V1 不建议在 ai_call_logs 中保存完整 prompt 和 API Key。
```

---

## 7. Android AI 架构

## 7.1 页面结构

```text
AiAssistantFragment
├── 顶部 Provider 状态
├── 消息列表 RecyclerView
├── 快捷操作区
│   ├── 生成 Markdown
│   ├── 提取待办
│   └── 文档总结入口（从文档详情进入时展示）
├── 输入框
└── 发送按钮
```

---

## 7.2 Android 分层

```text
ui/ai
├── AiAssistantFragment
├── AiViewModel
├── AiMessageAdapter
└── AiProviderSettingsActivity

data/repository
└── AiRepository

data/remote
└── AiApiService

model
├── AiChatRequest
├── AiChatResponse
├── AiProviderConfig
└── TodoSuggestion
```

---

## 7.3 Android 状态处理

| 状态 | UI 表现 |
|---|---|
| 未配置 Provider | 显示“请先配置 API Key” |
| 正在请求 AI | 输入框禁用，显示 loading |
| AI 返回成功 | 添加 assistant 消息 |
| AI 返回失败 | 显示错误提示，可重试 |
| 网络异常 | 显示网络错误 |
| Token 过期 | 跳转登录页 |

---

## 8. Web AI 架构

Web V1 可以做轻量版 AI 页面。

页面结构：

```text
AiView
├── Provider 状态
├── 输入框
├── 发送按钮
└── 回答结果区域
```

Web 的重点不是完整聊天体验，而是：

- 方便测试后端 AI 接口
- 方便长文本 Markdown 生成
- 方便用户在电脑端配置 Provider

---

## 9. AI 功能与其他模块关系

## 9.1 AI 与 Markdown

AI 可以：

- 总结当前 Markdown
- 根据需求生成 Markdown
- 优化 Markdown 表达
- 将 AI 回答保存为 Markdown 文档

---

## 9.2 AI 与待办

AI 可以：

- 从 Markdown 文档提取待办
- 从 AI 回答提取待办
- 从用户输入中生成待办建议

V1 推荐先让 AI 返回待办草稿，由用户确认后保存。

---

## 9.3 AI 与知识库

V1 中 AI 暂不直接读取整个知识库。

V2/V3 可以扩展为：

```text
用户选择一个知识库范围
→ 后端检索相关文档
→ 构造上下文
→ AI 回答
```

---

## 9.4 AI 与 CLI

V4 中 CLI 可以调用 AI 能力：

```bash
mkm ai chat "解释 JWT"
mkm ai summarize --doc 1
mkm ai markdown "生成 Spring Boot 学习笔记"
mkm ai todos --doc 1
```

因此 V1 的后端 AI API 应尽量保持 REST 友好和 JSON 结构清晰。

---

## 10. V1 推荐实现顺序

```text
1. Provider 配置表与接口
2. API Key 加密与脱敏展示
3. OpenAI-compatible Provider Adapter
4. AI 普通提问接口
5. Android AI 页面基础聊天
6. Markdown 生成接口
7. 文档总结接口
8. 待办提取接口
9. AI 回答保存为 Markdown
10. AI 提取待办并写入 todos
```

---

## 11. 当前结论

MKM AI V1 采用“可持续扩展 AI 助手”方案：

```text
Spring AI 封装
+ 用户自带 API Key
+ OpenAI-compatible Provider 优先
+ 支持 DeepSeek / MiniMax 等国内模型
+ 支持普通提问、Markdown 生成、文档总结、待办生成
+ 预留对话历史和调用日志
+ 暂不做全知识库 RAG
```

这套方案可以满足 V1 的产品体验，同时为 V2/V3 的知识库问答、RAG、CLI 和 Agent 调用预留扩展空间。
