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
本地优先 Markdown 知识库
+ Android 优质阅读和轻编辑
+ 每篇文档独立控制云端同步
+ BYOK AI 助手
+ 待办管理
+ Web 辅助管理
```

V1 不做社区化能力。

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
| 选择性同步 | 每篇文档独立控制是否同步云端 |
| AI 辅助整理 | 生成 Markdown、总结文档、提取待办 |
| Web 辅助管理 | 在浏览器管理已同步文档和待办 |

---

# 4. V1 产品边界

## 4.1 V1 必须包含

| 模块 | 必须能力 |
|---|---|
| 首次启动 | 创建 App 内知识库或打开系统文件夹知识库 |
| 知识库 | 文件夹树、卡片/列表视图、多知识库切换 |
| 本地模式 | 不登录可使用完整 Markdown 管理 |
| 登录注册 | 云同步、AI、Web 访问需要账号 |
| Markdown | 文档创建、编辑、预览、删除、移动、复制、导入、导出、搜索 |
| 标签 | 创建标签、打标签、按标签筛选 |
| 附件 | 图片/PDF/ZIP/常见附件，单文件 10MB 上限 |
| Todo | 标题、备注、日期、优先级、完成状态、关联文档 |
| AI | 单个 OpenAI 兼容配置、多会话、云端历史、问答、总结、生成、提取待办 |
| 我的 | 账号、AI Key、同步管理、主题、Markdown 渲染偏好、缓存、通知、关于 |
| Web | 登录、文件夹树、文档管理、搜索、标签、待办 |
| 后端 | Auth、Document、Attachment、Todo、AI Provider、AI Conversation |

---

## 4.2 V1 暂不包含

| 暂不做 | 原因 |
|---|---|
| 社区功能 | V1 专注个人知识库，不做评论/点赞/公开知识库浏览 |
| 推荐算法 | 属于社区阶段能力 |
| 复杂权限系统 | V1 以个人数据和同步为主 |
| 向量检索 RAG | V2/V3 再做 |
| 多 Provider 模板 | V1 只做一个 OpenAI 兼容配置 |
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
├── AiAssistantFragment
└── ProfileFragment
TodoActivity / TodoFragment（从日历入口进入）
DocumentDetailActivity
AiProviderSettingsActivity
SyncSettingsActivity
```

主导航：

```text
悬浮底部 3 Tab：Markdown / AI / 我的
```

---

## 5.2 Backend

Backend 负责账号、云同步、附件存储、AI Provider 配置和 AI 调用代理。

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
文档 CRUD + 文件夹结构
附件二进制存储（≤10MB）
Todo CRUD
AI Provider 配置加密保存
AI 请求转发
AI 多会话历史
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
知识库文件夹树
文档列表
文档编辑
Markdown 预览
搜索 + 标签
待办管理
```

Web V1 不做 AI 和复杂我的设置。

---

# 6. 启动与登录 Spec

## 6.1 启动原则

```text
知识库初始化优先于登录。
不登录也可以使用 App。
账号是可选增强能力。
云同步、AI、Web 多端访问需要账号。
```

---

## 6.2 首次启动流程

```text
首次启动
    │
    ├─ 创建 App 内知识库
    │       └─ 设置知识库名称 → 进入主页
    │
    └─ 打开系统文件夹知识库
            └─ 选择目录 → 校验权限 → 进入主页
```

登录只在用户触发云同步、AI 或账号相关能力时出现。

---

# 7. 知识库 Spec

## 7.1 知识库定义

知识库是 MKM 的 Markdown 文件容器。

```text
Repository
├── Markdown 文档
├── 文件夹结构
├── assets 附件
└── 同步状态
```

待办、AI 会话、我的设置是账号/全局级别，不随知识库完全隔离。

---

## 7.2 存储位置

| 类型 | 说明 | 适合用户 |
|---|---|---|
| App 内知识库 | App 私有目录，稳定安全 | 普通用户 |
| 系统文件夹知识库 | 用户选择目录，其他 App 可访问 | Markdown 重度用户 |

---

## 7.3 文件夹树

V1 必须保留目录结构：

```text
知识库根目录
├── Android/
│   ├── Room.md
│   └── Retrofit.md
├── Backend/
│   └── Spring Security.md
└── README.md
```

新建 Markdown 默认放在当前选中文件夹，无选中文件夹则放在根目录。

---

# 8. Markdown Spec

## 8.1 文档字段

```text
id
repositoryId
folderPath
fileName
title
content
tags
syncEnabled
syncStatus
localUpdatedAt
remoteUpdatedAt
createdAt
updatedAt
ownerId（云端）
```

---

## 8.2 文档能力

| 能力 | V1 要求 |
|---|---|
| 新建 | 支持标题、内容、标签，创建到当前文件夹 |
| 编辑 | 支持 Markdown 原文编辑 + 快捷工具栏 |
| 预览 | Markwon 渲染 |
| 删除 | App 内知识库进入回收站，系统文件夹知识库强提示风险 |
| 移动/复制 | 支持文件和文件夹 |
| 导入/导出 | 支持 `.md` 文件 |
| 分享 | 支持分享 `.md` 文件 |
| 搜索 | 文件名、标签、正文全文 |
| 筛选 | 标签筛选 |
| 视图 | 卡片视图、列表视图、文件夹树 |
| 同步 | 每篇文档独立控制是否云端同步 |

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
网络图片
相对路径本地图片
附件链接（PDF/ZIP/其他）
```

---

# 9. 附件 Spec

## 9.1 附件范围

```text
图片：支持网络图片 + 相对路径本地图片预览
附件：PDF / ZIP / 其他文件作为附件显示和打开
```

## 9.2 云端同步

```text
同步 Markdown + 全部附件
单文件最大 10MB
后端使用 PostgreSQL 二进制存储
```

## 9.3 引用策略

```text
本地 Markdown 原文保持相对路径
本地渲染时按知识库目录解析相对路径
云端/Web 渲染时由后端/API 把相对路径解析为附件下载地址
```

---

# 10. Todo Spec

## 10.1 Todo 字段

```text
id
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

Todo 全局共享，可关联 Markdown 文档。

---

## 10.2 Todo 能力

| 能力 | V1 要求 |
|---|---|
| 入口 | Markdown Toolbar → 日历弹窗 → 待办页 |
| 新建 | 标题、备注、日期、优先级、关联文档 |
| 完成/取消完成 | 点击圆圈切换 |
| 删除 | 支持删除确认或撤销 |
| 来源 | 用户手动创建 + AI 提取生成 |
| Web | Web 端保留待办管理 |

---

# 11. AI Spec

## 11.1 AI 原则

```text
V1 使用 BYOK 模式。
用户自带 API Key。
MKM 不代付模型费用。
后端加密保存 API Key。
必须登录且配置 Key 后才能使用 AI。
```

---

## 11.2 Provider 支持

V1 只支持一个 OpenAI 兼容配置：

```text
Base URL
API Key
Model 名称
```

兼容 OpenAI / DeepSeek / Kimi / 通义等 OpenAI-compatible 服务。

---

## 11.3 AI 功能

| 功能 | 说明 |
|---|---|
| 多会话管理 | 新建、切换、删除会话 |
| 对话历史 | 云端同步 |
| 普通提问 | 多轮对话 |
| 生成 Markdown | 根据主题生成结构化文档 |
| 文档总结 | 对当前 Markdown 生成摘要 |
| 提取待办 | 从文档或文本中提取 Todo 草稿 |
| 复制回答 | AI 回复可复制 |

---

## 11.4 API Key 安全

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

# 12. 账号与权限 Spec

## 12.1 账号可选

```text
本地模式不需要账号。
云同步、AI、Web 多端访问需要账号。
```

---

## 12.2 权限规则

| 资源 | 权限 |
|---|---|
| 云端文档 | 仅 owner 可读写 |
| 附件 | 仅 owner 可读写 |
| Todo | 仅 owner 可读写 |
| AI Provider | 仅 owner 可读写 |
| AI 会话 | 仅 owner 可读写 |

---

# 13. 同步 Spec

## 13.1 同步原则

```text
本地优先
每篇文档独立控制是否同步
开启同步的文档有网自动后台同步
失败不清空本地数据
Token 过期降级为本地模式
```

---

## 13.2 同步状态

| 状态 | 说明 |
|---|---|
| 仅本地 | 不上传云端 |
| 待同步 | 本地变更等待上传 |
| 同步中 | 正在上传/下载数据 |
| 已同步 | 本地和云端一致 |
| 同步失败 | 保留本地数据，提示用户重试 |
| 冲突 | 本地和云端都有修改，等待用户选择 |
| 登录过期 | 清除 Token，退回本地模式 |

---

## 13.3 冲突处理

```text
检测到冲突 → 弹窗提示 → 用户选择保留本地或保留云端 → 执行覆盖同步
```

V1 不做自动合并。

---

# 14. 安全 Spec

## 14.1 必须满足

```text
JWT 鉴权
密码 BCrypt 加密
API Key 加密保存
日志不打印敏感信息
资源必须校验 owner
AI 调用日志不保存 prompt 和 API Key
附件大小限制 10MB
```

---

## 14.2 禁止事项

```text
禁止数据库保存明文 API Key
禁止日志输出完整 API Key
禁止前端回显完整 API Key
禁止未授权访问私有文档/附件/Todo/AI 会话
禁止 AI 调用日志保存完整请求体
```

---

# 15. V1 验收标准

V1 完成时，必须能演示以下闭环：

## 15.1 本地知识库闭环

```text
首次启动 → 创建本地知识库 → 新建 Markdown → 保存 → 预览 → 编辑 → 再保存
```

## 15.2 文件夹管理闭环

```text
新建文件夹 → 在文件夹内创建文档 → 重命名 → 移动 → 删除 → 回收站恢复
```

## 15.3 同步闭环

```text
文档开启云端同步 → 登录 → 上传文档和附件 → Web 端查看同一文档
```

## 15.4 AI 闭环

```text
配置 OpenAI 兼容 API Key → 新建会话 → 提问 → 获得回答 → 切换历史会话
```

## 15.5 文档总结闭环

```text
打开文档 → AI 总结 → 查看摘要 → 提取待办 → 保存 Todo
```

## 15.6 Todo 闭环

```text
Toolbar 日历 → 进入待办 → 创建待办 → 设置优先级和日期 → 标记完成
```

## 15.7 Web 管理闭环

```text
Web 登录 → 查看文件夹树 → 新建文档 → Android 同步后看到
```

---

# 16. 当前结论

MKM V1 的基础 Spec 可以概括为：

```text
本地优先
知识库优先
账号可选
选择性云同步
Android 主体验
AI BYOK
Web 辅助管理
不做社区功能
```

V1 优先跑通：

```text
知识库初始化
Markdown 阅读编辑
文件夹树管理
选择性同步
Todo 管理
AI 辅助整理
Web 文档和待办管理
```
