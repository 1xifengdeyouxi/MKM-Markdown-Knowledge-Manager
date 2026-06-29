# MKM 模块规格说明

## 1. 文档目标

本文档用于定义 MKM V1 的核心模块规格，作为后续开发时的产品与技术参考。

覆盖内容：

- 模块职责
- 页面结构
- 数据模型
- API 接口
- Android 实现建议
- Web 实现建议
- AI 模块 Provider 设计

V1 采用：

```text
Android 优先 + Spring Boot 后端 + Vue 3 Web 辅助端 + Spring AI 封装
```

---

## 2. 模块总览

MKM V1 包含 5 个一级模块：

```text
Markdown    待办    AI    知识库    我的
```

| 模块 | V1 定位 | 优先级 |
|---|---|---|
| Markdown | 核心知识库与文档管理 | P0 |
| 待办 | 承接文档和 AI 生成的行动项 | P1 |
| AI | 提问、总结、生成 Markdown、提取待办 | P1 |
| 知识库 | 公开文档和示例知识库入口 | P1 |
| 我的 | 账号、同步、AI Key、设置 | P0 |

---

# 3. Markdown 模块规格

## 3.1 模块职责

Markdown 模块负责用户个人文档的创建、查看、编辑、删除和渲染。

V1 的核心是：

```text
数据库中的 Markdown 文档 → Android / Web 展示 → 可编辑 → 可同步
```

---

## 3.2 页面结构

### Android 页面

```text
MarkdownFragment
├── 顶部搜索框 / 标题栏
├── 文档列表 RecyclerView
├── 新建按钮 FAB
└── 文档详情页 DocumentDetailActivity
    ├── 标题
    ├── Markdown 渲染区
    ├── 编辑按钮
    ├── AI 总结按钮
    └── 生成待办按钮
```

### Web 页面

```text
DocumentListView
├── 文档列表
├── 新建按钮
└── 文档详情 DocumentDetailView
    ├── Markdown 预览
    └── 编辑区域
```

---

## 3.3 数据模型

### documents

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| title | String | 文档标题 |
| content | Text | Markdown 内容 |
| summary | Text? | AI 摘要，可选 |
| isPublic | Boolean | 是否公开 |
| ownerId | Long | 所属用户 |
| tags | String? | 简单标签，V1 可用逗号分隔 |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

V1 可以先不单独建 tags 表，后续再扩展为：

```text
tags
document_tags
```

---

## 3.4 API 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/documents | 获取我的文档列表 |
| GET | /api/documents/{id} | 获取文档详情 |
| POST | /api/documents | 创建文档 |
| PUT | /api/documents/{id} | 更新文档 |
| DELETE | /api/documents/{id} | 删除文档 |
| GET | /api/documents/public | 获取公开文档列表 |
| PUT | /api/documents/{id}/visibility | 修改公开状态，可选 |

---

## 3.5 Android 实现建议

| 层 | 类 |
|---|---|
| UI | MarkdownFragment / DocumentDetailActivity |
| ViewModel | DocumentViewModel |
| Repository | DocumentRepository |
| Remote | DocumentApiService |
| Local | Room DocumentDao |
| Render | Markwon |

Markdown 渲染优先支持：

- GFM 表格
- 代码块
- 链接
- 图片
- 任务列表

---

# 4. 待办模块规格

## 4.1 模块职责

待办模块负责管理用户的任务，也承接 AI 从文档中提取出来的行动项。

---

## 4.2 页面结构

### Android 页面

```text
TodoFragment
├── 今日待办
├── 全部待办
├── 已完成
├── 新建待办按钮
└── 待办编辑弹窗 / 页面
```

### Web 页面

V1 可选实现：

```text
TodoView
├── 待办列表
└── 新建待办
```

---

## 4.3 数据模型

### todos

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| title | String | 待办标题 |
| description | Text? | 描述 |
| completed | Boolean | 是否完成 |
| priority | String? | low / medium / high |
| dueDate | DateTime? | 截止时间 |
| sourceType | String? | manual / document / ai |
| sourceDocumentId | Long? | 来源文档 |
| ownerId | Long | 所属用户 |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

---

## 4.4 API 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/todos | 获取我的待办 |
| POST | /api/todos | 创建待办 |
| PUT | /api/todos/{id} | 更新待办 |
| PUT | /api/todos/{id}/complete | 标记完成 |
| PUT | /api/todos/{id}/uncomplete | 取消完成 |
| DELETE | /api/todos/{id} | 删除待办 |

---

# 5. AI 模块规格

## 5.1 模块职责

AI 模块用于增强知识库能力，V1 主要支持：

- 普通 AI 提问
- 当前文档总结
- 生成 Markdown 草稿
- 从文档或 AI 回答中提取待办

AI 是 MKM 的中间主入口，但 V1 不做复杂 Agent 和完整 RAG。

---

## 5.2 AI 接入原则

MKM 采用：

```text
Spring AI 封装 + 用户自带 API Key + 多 Provider 适配
```

也就是 BYOK：Bring Your Own Key。

用户可以在“我的 → AI 设置”中添加自己的模型配置。

---

## 5.3 支持的 Provider 策略

### V1 推荐优先级

| 优先级 | Provider 类型 | 示例 |
|---|---|---|
| P0 | OpenAI-compatible 国内模型 | DeepSeek、MiniMax、通义千问、智谱等兼容接口 |
| P1 | 标准 OpenAI 接口 | OpenAI |
| P1 | Anthropic / Claude | 后续可接 |
| P2 | 本地模型 | Ollama，后续可接 |

V1 最建议先按 **OpenAI-compatible API** 抽象一层，因为很多国内模型提供类似 OpenAI 的接口格式。

这样后端不需要为每个模型写一套完整逻辑，只需要保存：

```text
baseUrl + apiKey + modelName + providerType
```

---

## 5.4 用户 AI Key 配置

### 页面入口

```text
我的
└── 设置
    └── AI 设置
        ├── 添加 Provider
        ├── 选择默认模型
        ├── 测试连接
        └── 删除配置
```

### 用户可填写字段

| 字段 | 说明 | 示例 |
|---|---|---|
| providerName | 显示名称 | DeepSeek |
| providerType | 类型 | openai-compatible |
| baseUrl | API 地址 | https://api.deepseek.com |
| apiKey | 用户自己的 Key | sk-xxxx |
| modelName | 模型名称 | deepseek-chat |
| enabled | 是否启用 | true |
| defaultProvider | 是否默认 | true |

---

## 5.5 API Key 安全原则

API Key 不能明文存储。

V1 建议：

1. 后端接收用户 API Key。
2. 后端使用服务端密钥加密后保存。
3. 前端和 Android 不再回显完整 Key。
4. 再次展示时只显示脱敏结果。

示例：

```text
sk-abc123456789 → sk-abc****789
```

后续上线前需要加强：

- 服务端加密密钥放环境变量
- 禁止日志打印 API Key
- AI 请求日志不记录敏感信息
- 支持用户删除 API Key

---

## 5.6 数据模型

### ai_provider_configs

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| ownerId | Long | 所属用户 |
| providerName | String | 显示名称 |
| providerType | String | openai-compatible / openai / anthropic / ollama |
| baseUrl | String | API 地址 |
| encryptedApiKey | Text | 加密后的 API Key |
| modelName | String | 默认模型 |
| enabled | Boolean | 是否启用 |
| defaultProvider | Boolean | 是否默认 |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

### ai_conversations（可选）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| ownerId | Long | 所属用户 |
| title | String | 对话标题 |
| providerConfigId | Long? | 使用的 Provider |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

### ai_messages（可选）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| conversationId | Long | 所属对话 |
| role | String | user / assistant / system |
| content | Text | 消息内容 |
| createdAt | DateTime | 创建时间 |

V1 若要降低复杂度，可以先不保存对话，只保存 provider 配置。

---

## 5.7 AI API 接口

### Provider 配置接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/ai/providers | 获取我的 Provider 配置列表 |
| POST | /api/ai/providers | 添加 Provider 配置 |
| PUT | /api/ai/providers/{id} | 更新 Provider 配置 |
| DELETE | /api/ai/providers/{id} | 删除 Provider 配置 |
| POST | /api/ai/providers/{id}/test | 测试 Provider 是否可用 |
| PUT | /api/ai/providers/{id}/default | 设为默认 Provider |

### AI 能力接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/ai/chat | 普通 AI 提问 |
| POST | /api/ai/markdown/generate | 生成 Markdown 草稿 |
| POST | /api/ai/documents/{id}/summarize | 总结当前文档 |
| POST | /api/ai/documents/{id}/todos | 从文档提取待办 |
| POST | /api/ai/text/todos | 从任意文本提取待办 |

---

## 5.8 Prompt 模板

### 普通提问

```text
你是 MKM 的 AI 助手，面向程序员和技术学习者。
请用清晰、结构化的方式回答用户问题。
如果问题涉及代码或技术步骤，请尽量给出示例和注意事项。
```

### 文档总结

```text
请总结以下 Markdown 文档，输出结构如下：

1. 核心摘要
2. 关键知识点
3. 重要代码 / 命令 / 配置
4. 可以转化为待办的行动项
5. 推荐复习点

文档内容：
{{documentContent}}
```

### Markdown 生成

```text
请根据用户需求生成一份 Markdown 文档。
要求：
- 标题层级清晰
- 使用列表和表格组织信息
- 如果适合，请加入代码块
- 输出纯 Markdown，不要额外解释

用户需求：
{{userInput}}
```

### 待办提取

```text
请从以下内容中提取可以执行的待办事项。
每条待办应简短、明确、可执行。

输出格式：
- [ ] 待办 1
- [ ] 待办 2

内容：
{{content}}
```

---

## 5.9 Android AI 页面建议

```text
AiAssistantFragment
├── Provider 状态提示
├── 聊天消息列表
├── 输入框
├── 发送按钮
└── 快捷操作
    ├── 总结当前文档
    ├── 生成 Markdown
    ├── 提取待办
    └── 保存为文档
```

如果用户未配置 API Key：

```text
显示空状态：
“请先在 我的 → AI 设置 中配置模型 API Key”
按钮：去配置
```

---

# 6. 知识库模块规格

## 6.1 模块职责

知识库模块用于展示公开文档和示例知识库，是未来社区化能力的入口。

---

## 6.2 V1 简化设计

V1 不单独做复杂 knowledge_base 表，可以先将公开文档作为知识库内容：

```text
更多知识库 = 公开文档列表 + 示例文档
```

V3 再升级为：

```text
knowledge_bases
knowledge_base_documents
comments
likes
favorites
```

---

## 6.3 页面结构

```text
KnowledgeBaseFragment
├── 示例知识库
├── 公开文档
├── 热门占位
└── 文档详情只读页
```

---

## 6.4 API 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/documents/public | 获取公开文档 |
| GET | /api/documents/{id} | 查看公开文档详情 |
| PUT | /api/documents/{id}/visibility | 设置文档公开，可选 |

---

# 7. 我的模块规格

## 7.1 模块职责

我的模块负责账号、设置、同步状态、AI Key 配置和产品信息。

---

## 7.2 页面结构

```text
ProfileFragment
├── 用户信息
├── 同步状态
├── AI 设置
├── Markdown 设置
├── 关于 MKM
└── 退出登录
```

---

## 7.3 V1 必做功能

- 显示用户名
- 退出登录
- AI Provider 配置入口
- 同步状态展示
- 关于页面

---

## 7.4 API 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/users/me | 获取当前用户信息，可选 |
| GET | /api/ai/providers | 获取 AI 配置 |
| POST | /api/ai/providers | 添加 AI 配置 |
| DELETE | /api/ai/providers/{id} | 删除 AI 配置 |

---

# 8. 后端包结构建议

V1 后端建议按业务模块拆包：

```text
com.mkm
├── auth
│   ├── AuthController
│   ├── AuthService
│   └── dto
├── document
│   ├── DocumentController
│   ├── DocumentService
│   ├── DocumentRepository
│   └── model
├── todo
│   ├── TodoController
│   ├── TodoService
│   ├── TodoRepository
│   └── model
├── ai
│   ├── AiController
│   ├── AiService
│   ├── AiProviderConfigService
│   ├── AiProviderConfigRepository
│   ├── ProviderAdapter
│   └── dto
├── user
│   ├── UserController
│   ├── UserRepository
│   └── model
└── common
    ├── security
    ├── exception
    └── config
```

如果前期项目较小，也可以继续使用当前 controller/service/repository/model 分层，后续再重构为模块化包结构。

---

# 9. Android 包结构建议

```text
com.mkm.android
├── data
│   ├── local
│   ├── remote
│   └── repository
├── model
├── ui
│   ├── auth
│   ├── markdown
│   ├── todo
│   ├── ai
│   ├── knowledge
│   └── profile
└── util
```

当前项目中 document 可以逐步改名为 markdown，使产品语言更统一。

---

# 10. Web 端结构建议

```text
src
├── api
│   ├── auth.js
│   ├── document.js
│   ├── todo.js
│   └── ai.js
├── stores
│   ├── auth.js
│   ├── document.js
│   ├── todo.js
│   └── ai.js
├── views
│   ├── LoginView.vue
│   ├── DocumentListView.vue
│   ├── DocumentDetailView.vue
│   ├── TodoView.vue
│   ├── AiView.vue
│   └── ProfileView.vue
└── components
    ├── MarkdownRenderer.vue
    ├── AiProviderForm.vue
    └── TodoItem.vue
```

---

# 11. V1 开发任务拆分建议

## Phase 1：基础闭环

- Auth 登录注册
- Document CRUD
- Android Markdown 渲染
- Web 文档管理

## Phase 2：五 Tab 补齐

- Android 底部导航改为 Markdown / 待办 / AI / 知识库 / 我的
- Todo 数据表和 API
- Todo Android 页面
- 知识库公开文档列表
- 我的页面基础设置

## Phase 3：AI 接入

- ai_provider_configs 表
- AI Key 加密存储
- Provider 配置页面
- Spring AI ChatClient 封装
- OpenAI-compatible Provider 适配
- DeepSeek / MiniMax 等国内模型配置示例
- AI 普通提问
- 当前文档总结

## Phase 4：体验增强

- AI 回答保存为 Markdown
- AI 提取待办
- 文档搜索
- 文档标签
- 同步状态提示

---

# 12. 当前结论

MKM V1 的模块规格确定为：

```text
Markdown 是基础内容层
待办是行动承接层
AI 是智能增强层
知识库是公开分享入口
我的是账号与配置中心
```

AI 接入方案确定为：

```text
Spring AI 封装 + 用户自带 API Key + OpenAI-compatible Provider 优先
```

用户可以在“我的 → AI 设置”中添加 DeepSeek、MiniMax 等国内模型的 API Key，并选择默认模型。

V1 不追求复杂 RAG，而是先跑通：

```text
配置模型 → 普通提问 → 总结文档 → 生成 Markdown → 提取待办
```
