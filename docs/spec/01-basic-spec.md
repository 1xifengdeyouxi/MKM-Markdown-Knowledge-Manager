# MKM 基础 Spec 说明

## 1. 文档目标

本文档是 MKM（Markdown Knowledge Manager）的基础规格说明文件，用于统一项目从产品设计到技术实现的核心约束。

它回答以下问题：

```text
MKM 是什么？
V1 要做什么？
哪些能力必须具备？
哪些能力暂时不做？
Android、后端、Web、AI 各自承担什么职责？
```

---

# 2. 产品一句话定义

MKM 是一个面向程序员和技术学习者的移动端优先 Markdown 知识库工具。

核心形态：

```text
本地优先 Markdown 仓库
+ Android 优质阅读和轻编辑
+ 可选账号云同步
+ BYOK AI 助手
+ 待办管理
+ 公开知识库浏览
+ Web 辅助管理
```

---

# 3. 目标用户

## 3.1 V1 核心用户

```text
开发者本人
技术朋友
Android 技术用户
Markdown 重度用户
技术学习者
```

## 3.2 用户核心需求

| 需求 | 说明 |
|---|---|
| 手机查看 Markdown | 在 Android 上清晰阅读 Markdown 文档 |
| 管理个人知识库 | 创建、编辑、搜索、整理技术笔记 |
| 本地优先 | 不登录也能使用，数据可保存在设备上 |
| 可选同步 | 需要多端同步时再登录账号 |
| AI 辅助整理 | 生成 Markdown、总结文档、提取待办 |
| 公开知识库 | 浏览他人公开的技术文档 |

---

# 4. V1 产品边界

## 4.1 V1 必须包含

| 模块 | 必须能力 |
|---|---|
| 首次启动 | 选择创建仓库或使用现有仓库 |
| 仓库 | 支持仓库名称、仓库存储位置、初始化状态 |
| 本地模式 | 不登录可使用基础 Markdown 管理 |
| 登录注册 | 云同步、评论、点赞等需要账号的能力 |
| Markdown | 文档创建、编辑、预览、删除、搜索 |
| Todo | 创建、完成、删除、优先级、截止时间、来源文档 |
| AI | 配置用户 API Key、普通提问、生成 Markdown、总结、提取待办 |
| 知识库 | 浏览公开文档、搜索、标签筛选、评论 |
| 我的 | 仓库信息、同步状态、AI 设置、主题、语言、导出 |
| Web | 登录、文档列表、文档编辑和预览 |
| 后端 | Auth、Document、Todo、AI Provider、Public Knowledge API |

---

## 4.2 V1 暂不包含

| 暂不做 | 原因 |
|---|---|
| 多仓库切换 | V1 先支持单仓库，降低复杂度 |
| 团队空间 | 属于 V4 远期能力 |
| 复杂权限系统 | V1 仅私有/公开两种状态 |
| 图片上传服务 | V1 可先支持本地 assets，云端图片后置 |
| 评论回复楼中楼 | V1 只做一级评论 |
| AI 平台代付费 | V1 坚持 BYOK，用户自带 API Key |
| 支付系统 | V1 免费验证，不商业化 |
| CLI Agent 调用 | 远期能力，V1 只预留方向 |

---

# 5. 平台范围

## 5.1 Android

Android 是 V1 主产品端。

技术要求：

```text
Kotlin
XML Layout
Material Components
Navigation Component
Retrofit + OkHttp
Room / DataStore
Markwon
```

核心页面：

```text
OnboardingActivity
LoginActivity（可选登录）
MainActivity
├── MarkdownFragment
├── TodoFragment
├── AiAssistantFragment
├── KnowledgeFragment
└── ProfileFragment
DocumentDetailActivity
AiProviderSettingsActivity
```

---

## 5.2 Backend

Backend 负责账号、云同步、公开知识库、AI Provider 配置和 AI 调用代理。

技术要求：

```text
Spring Boot 3.x
Kotlin
Spring Security
JWT
Spring Data JPA
PostgreSQL
```

核心职责：

```text
用户注册登录
文档 CRUD
Todo CRUD
公开文档浏览
评论和点赞
AI Provider 配置加密保存
AI 请求转发
同步数据存储
```

---

## 5.3 Web

Web 是辅助管理端，不是 V1 的主体验端。

技术要求：

```text
Vue 3
Vite
Pinia
Vue Router
Axios
marked.js
DOMPurify
```

V1 Web 能力：

```text
登录注册
文档列表
文档编辑
Markdown 预览
基础同步验证
```

Todo、AI、知识库 Web 端可以后续补齐。

---

# 6. 启动与登录 Spec

## 6.1 启动原则

```text
仓库初始化优先于登录。
不登录也可以使用 App。
账号是可选能力。
云同步、评论、点赞需要账号。
```

---

## 6.2 首次启动流程

```text
首次启动
    │
    ├─ 创建新仓库
    │       ├─ 开启云同步 → 登录/注册 → 设置仓库信息 → 进入主页
    │       └─ 跳过同步 → 设置仓库信息 → 进入主页
    │
    └─ 使用现有仓库
            ├─ 打开本地仓库 → 选择目录 → 校验 → 进入主页
            └─ 从账号恢复 → 登录/注册 → 选择云端仓库 → 同步 → 进入主页
```

---

## 6.3 登录出现时机

LoginActivity 只在以下场景出现：

```text
开启云同步
从账号恢复仓库
未登录时尝试评论
未登录时尝试点赞
在我的页面点击登录
```

---

# 7. 仓库 Spec

## 7.1 仓库定义

仓库是 MKM 的核心数据容器。

```text
Repository
├── Markdown 文档
├── Todo 数据
├── AI 生成结果
├── assets 附件
├── 本地配置
└── 同步状态
```

---

## 7.2 存储位置

| 类型 | 说明 | 适合用户 |
|---|---|---|
| 设备存储 | 其他应用可以访问，适合文件管理器/电脑同步 | Markdown 重度用户 |
| 应用存储 | App 私有目录，其他应用不能访问，卸载后数据删除 | 普通用户 |

V1 默认推荐：

```text
设备存储
```

原因：

```text
更接近 Obsidian Vault 心智
Markdown 文件对用户可见
更符合本地优先工具定位
```

---

## 7.3 仓库初始化结构

```text
MKM-Repository/
├── documents/
├── todos/
├── assets/
├── .mkm/
│   ├── config.json
│   ├── repository.json
│   └── sync.json
└── README.md
```

---

# 8. Markdown Spec

## 8.1 文档字段

```text
id
repositoryId
title
content
tags
isPublic
createdAt
updatedAt
ownerId（云端）
```

---

## 8.2 文档能力

| 能力 | V1 要求 |
|---|---|
| 新建 | 支持标题、内容、标签 |
| 编辑 | 支持 Markdown 原文编辑 |
| 预览 | Markwon 渲染 |
| 删除 | 支持确认删除 |
| 搜索 | 标题、标签、正文（正文可选） |
| 排序 | 更新时间、创建时间、文件名 |
| 筛选 | 全部、私有、公开、标签 |
| 视图 | 列表视图、宫格视图 |
| 导出 | 支持导出 Markdown 文件 |
| 公开 | 支持私有/公开切换 |

---

## 8.3 Markdown 渲染支持

V1 至少支持：

```text
标题 H1-H6
段落
加粗 / 斜体 / 删除线
代码块
行内代码
引用
有序列表 / 无序列表
任务列表
表格
链接
图片（本地 assets，云端图片后续）
```

---

# 9. Todo Spec

## 9.1 Todo 字段

```text
id
repositoryId
title
note
priority: low / medium / high
dueDate
completed
sourceDocumentId
sourceDocumentTitle
createdAt
updatedAt
completedAt
```

---

## 9.2 Todo 能力

| 能力 | V1 要求 |
|---|---|
| 新建 | 内容、备注、优先级、截止时间 |
| 完成/取消完成 | 点击圆圈切换 |
| 删除 | 支持滑动删除和撤销 |
| 来源文档 | AI 提取的待办可关联来源文档 |
| 详情 | BottomSheet 展示详细信息 |
| Tab | 未完成 / 已完成 |
| 排序 | 创建时间、截止时间、优先级 |

---

# 10. AI Spec

## 10.1 AI 原则

```text
V1 使用 BYOK 模式。
用户自带 API Key。
MKM 不代付模型费用。
后端加密保存 API Key。
AI 输出优先结构化 Markdown。
```

---

## 10.2 Provider 支持

V1 Provider：

```text
DeepSeek
MiniMax
通义千问
智谱 GLM
OpenAI
自定义 OpenAI-compatible Base URL
```

---

## 10.3 AI 功能

| 功能 | 说明 |
|---|---|
| 普通提问 | 多轮对话 |
| 生成 Markdown | 根据主题生成结构化文档 |
| 文档总结 | 对当前 Markdown 生成摘要 |
| 提取待办 | 从文档或文本中提取 Todo 草稿 |
| 保存为文档 | AI 回复可保存为 Markdown |
| 提取为 Todo | AI 回复可转为待办 |

---

## 10.4 API Key 安全

必须满足：

```text
不在前端长期保存完整 API Key
不在日志中打印 API Key
数据库不保存明文 API Key
返回前端时只返回脱敏结果
用户可以删除配置
```

后端环境变量：

```text
AI_KEY_ENCRYPTION_SECRET=至少32位随机字符串
```

---

# 11. 公开知识库 Spec

## 11.1 公开文档

用户可以将自己的文档设为公开。

公开文档可被其他用户浏览。

---

## 11.2 知识库页面能力

| 能力 | V1 要求 |
|---|---|
| 公开文档列表 | 展示所有公开文档 |
| 搜索 | 搜索公开文档标题和标签 |
| 标签筛选 | 按技术标签筛选 |
| 只读详情 | 公开文档只读展示 |
| 点赞 | 登录用户可点赞 |
| 评论 | 登录用户可发表评论 |
| AI 总结 | 可对公开文档做 AI 总结 |

---

# 12. 账号与权限 Spec

## 12.1 账号可选

```text
本地模式不需要账号。
云同步、评论、点赞、公开文档发布需要账号。
```

---

## 12.2 权限规则

| 资源 | 权限 |
|---|---|
| 私有文档 | 仅 owner 可读写 |
| 公开文档 | 所有人可读，owner 可写 |
| Todo | 仅 owner 可读写 |
| AI Provider | 仅 owner 可读写 |
| 评论 | 所有人可读，登录用户可写，作者可删自己的评论 |

---

# 13. 同步 Spec

## 13.1 同步原则

```text
本地优先
后台同步
失败不清空本地数据
Token 过期降级为本地模式
```

---

## 13.2 同步状态

| 状态 | 说明 |
|---|---|
| 未开启 | 纯本地模式 |
| 同步中 | 正在上传/下载数据 |
| 已同步 | 本地和云端一致 |
| 同步失败 | 保留本地数据，提示用户重试 |
| 登录过期 | 清除 Token，退回本地模式 |

---

# 14. 安全 Spec

## 14.1 必须满足

```text
JWT 鉴权
密码 BCrypt 加密
API Key 加密保存
日志不打印敏感信息
资源必须校验 owner
公开文档必须用户主动设置
AI 调用日志不保存 prompt 和 API Key
```

---

## 14.2 禁止事项

```text
禁止数据库保存明文 API Key
禁止日志输出完整 API Key
禁止前端回显完整 API Key
禁止未授权访问私有文档
禁止 AI 调用日志保存完整请求体
```

---

# 15. V1 验收标准

V1 完成时，必须能演示以下闭环：

## 15.1 本地仓库闭环

```text
首次启动 → 创建本地仓库 → 新建 Markdown → 保存 → 预览 → 编辑 → 再保存
```

## 15.2 同步闭环

```text
本地仓库 → 开启云同步 → 登录 → 上传文档 → Web 端查看同一文档
```

## 15.3 AI 闭环

```text
配置 DeepSeek API Key → 提问 → 获得回答 → 保存为 Markdown
```

## 15.4 文档总结闭环

```text
打开文档 → AI 总结 → 查看摘要 → 提取待办 → 保存 Todo
```

## 15.5 Todo 闭环

```text
创建待办 → 设置优先级和截止时间 → 标记完成 → 查看已完成
```

## 15.6 公开知识库闭环

```text
设置文档公开 → 进入知识库 → 搜索公开文档 → 查看详情 → 评论
```

---

# 16. 当前结论

MKM V1 的基础 Spec 可以概括为：

```text
本地优先
仓库优先
账号可选
云同步增强
Android 主体验
AI BYOK
Markdown + Todo + Knowledge 三条核心链路
```

V1 不追求功能大而全，而是优先跑通：

```text
仓库初始化
Markdown 阅读编辑
Todo 管理
AI 辅助整理
公开知识库浏览
可选云同步
```
