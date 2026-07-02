# MKM 项目文档总索引

MKM（Markdown Knowledge Manager）是一个面向程序员和技术学习者的移动端优先 Markdown 知识库项目。

项目目标：

```text
Android Markdown 知识库
+ AI 助手
+ 待办管理
+ Web 辅助端
+ 后端数据库同步
+ 未来公开知识库与 CLI Agent 调用
```

---

## 1. 推荐阅读顺序

如果你是第一次阅读，建议按以下顺序：

```text
0. deployment/00-dev-environment.md   ← 开发环境配置（先看这个）
1. spec/01-basic-spec.md
2. spec/02-android-spec.md
3. spec/03-backend-spec.md
4. spec/04-web-spec.md
5. product/01-product-positioning.md
6. product/02-feature-roadmap.md
7. product/03-mvp-scope.md
8. product/04-user-flows.md
9. product/05-module-specs.md
10. technical/01-ai-architecture.md
11. technical/02-ai-provider-config.md
12. technical/03-ai-prompts.md
13. technical/04-ai-feature-flows.md
14. api/01-data-models.md
15. api/02-api-spec.md
16. design/01-android-ui.md
17. design/02-state-flows.md
18. design/03-onboarding-repository-setup.md
19. design/04-visual-system.md
20. deployment/01-launch-and-deployment.md
21. deployment/02-commercialization-growth.md
```

---

## 0. 基础 Spec 文档

目录：`docs/spec/`

| 文档 | 说明 |
|---|---|
| [01-basic-spec.md](spec/01-basic-spec.md) | 基础规格说明：产品边界、平台分工、功能 Spec、安全、验收标准 |
| [02-android-spec.md](spec/02-android-spec.md) | Android 端规范：页面、数据、本地仓库、状态、安全、验收 |
| [03-backend-spec.md](spec/03-backend-spec.md) | Backend 端规范：模块职责、API、数据库、安全、部署、验收 |
| [04-web-spec.md](spec/04-web-spec.md) | Web 端规范：页面、路由、状态管理、Markdown 渲染、安全、部署 |

### Spec 核心结论

V1 的基础原则：

```text
本地优先 · 仓库优先 · 账号可选
Android 主体验 · Web 辅助管理
AI BYOK · 不代付 AI 费用
Markdown + Todo + Knowledge 三条核心链路
V1 不商业化，先验证核心使用价值
```

---



目录：`docs/product/`

| 文档 | 说明 |
|---|---|
| [01-product-positioning.md](product/01-product-positioning.md) | 产品定位、目标用户、核心痛点、Obsidian 参考与差异 |
| [02-feature-roadmap.md](product/02-feature-roadmap.md) | V1/V2/V3/V4 功能路线图，底部导航设计 |
| [03-mvp-scope.md](product/03-mvp-scope.md) | V1 增强 MVP 范围，哪些做、哪些不做 |
| [04-user-flows.md](product/04-user-flows.md) | 登录、文档、AI、待办、知识库、我的等用户流程 |
| [05-module-specs.md](product/05-module-specs.md) | Markdown、待办、AI、知识库、我的模块规格 |

### 核心结论

MKM 的主定位：

```text
面向程序员的移动端优先个人 Markdown 知识库。
```

V1 底部导航：

```text
Markdown    待办    AI    知识库    我的
```

---

## 3. AI 技术设计文档

目录：`docs/technical/`

| 文档 | 说明 |
|---|---|
| [01-ai-architecture.md](technical/01-ai-architecture.md) | AI 总体架构，Spring AI + BYOK + Provider Adapter |
| [02-ai-provider-config.md](technical/02-ai-provider-config.md) | 用户 API Key 配置、DeepSeek/MiniMax/通义/智谱等支持方式 |
| [03-ai-prompts.md](technical/03-ai-prompts.md) | Prompt 模板：普通提问、Markdown 生成、文档总结、待办提取 |
| [04-ai-feature-flows.md](technical/04-ai-feature-flows.md) | AI 端到端流程：提问、生成文档、总结、提取待办 |

### AI 核心结论

AI 方案：

```text
Spring AI 封装
+ 用户自带 API Key（BYOK）
+ OpenAI-compatible Provider 优先
+ 支持 DeepSeek / MiniMax / 通义 / 智谱等国内模型
```

V1 AI 核心功能：

```text
普通提问
Markdown 生成
文档总结
待办提取
```

---

## 4. 数据模型与 API 文档

目录：`docs/api/`

| 文档 | 说明 |
|---|---|
| [01-data-models.md](api/01-data-models.md) | PostgreSQL 数据表设计：users/documents/todos/AI 配置等 |
| [02-api-spec.md](api/02-api-spec.md) | REST API 规范，Auth/Document/Todo/AI/User 接口 |

### 数据模型核心结论

V1 主要表：

```text
users
documents
todos
ai_provider_configs
ai_conversations
ai_messages
ai_call_logs
```

V1 API 覆盖：

```text
Auth
Document
Todo
AI Provider
AI 功能
User
```

---

## 5. Android 体验设计文档

目录：`docs/design/`

| 文档 | 说明 |
|---|---|
| [01-android-ui.md](design/01-android-ui.md) | Android 页面结构、布局草图、BottomNav、弹窗、AI 设置页面 |
| [02-state-flows.md](design/02-state-flows.md) | 页面状态机、导航流转、Token 过期、网络异常处理 |
| [03-onboarding-repository-setup.md](design/03-onboarding-repository-setup.md) | 首次启动流程、仓库初始化、存储位置选择、语言选择 |
| [04-visual-system.md](design/04-visual-system.md) | 颜色系统、字体层级、间距、圆角、动效、组件样式规范 |

### Android 核心页面

```text
OnboardingActivity（首次启动）
LoginActivity（可选，仅同步时出现）
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

## 6. 上线与商业化文档

目录：`docs/deployment/`

| 文档 | 说明 |
|---|---|
| [00-dev-environment.md](deployment/00-dev-environment.md) | **开发环境配置**：JDK、Android Studio、Node.js、Docker 安装和版本说明 |
| [01-launch-and-deployment.md](deployment/01-launch-and-deployment.md) | 本地开发、服务器部署、Docker、Nginx、HTTPS、备份、Android 发布 |
| [02-commercialization-growth.md](deployment/02-commercialization-growth.md) | 商业化路线、定价草案、增长路径、风险分析 |

### 上线核心结论

推荐上线阶段：

```text
本地开发可运行
→ 个人服务器测试环境
→ 小范围朋友内测
→ 正式产品化上线
```

推荐商业模式：

```text
BYOK AI + 云同步收费 + Pro 高级功能
```

---

## 7. V1 推荐开发顺序

建议按以下顺序推进：

```text
1. Auth 登录注册
2. Markdown 文档 CRUD
3. Android Markdown 渲染
4. Web 文档管理
5. Todo 待办 CRUD
6. AI Provider 配置
7. AI 普通提问
8. Markdown 生成
9. 当前文档 AI 总结
10. AI 提取待办
11. 更多知识库公开文档
12. 我的页面和同步状态
```

---

## 8. V1 核心验收闭环

V1 完成时，需要能演示以下流程：

### Markdown 闭环

```text
注册/登录 → 新建 Markdown → 保存 → 查看渲染 → 编辑 → 再保存
```

### 同步闭环

```text
Web 创建文档 → 后端保存 → Android 刷新后看到同一篇文档
```

### AI 闭环

```text
配置 DeepSeek API Key → 提问 → 获得回答 → 保存为 Markdown
```

### 文档总结闭环

```text
打开文档 → AI 总结 → 查看摘要 → 提取待办
```

### 待办闭环

```text
创建待办 → 标记完成 → 查看已完成
```

### 知识库闭环

```text
进入知识库 → 查看公开文档 → 正常渲染 Markdown
```

---

## 9. 项目当前技术栈

| 模块 | 技术 |
|---|---|
| Android | Kotlin + XML + Markwon + Retrofit + Room |
| Backend | Spring Boot Kotlin + PostgreSQL + JWT |
| Web | Vue 3 + Vite + Pinia |
| AI | Spring AI / OpenAI-compatible Provider |
| 部署 | Docker Compose + Nginx + PostgreSQL |

---

## 10. 项目总目标

MKM 的长期形态：

```text
一个面向程序员的移动端优先 Markdown 知识库产品，
支持 Android 端优质阅读与轻整理，
支持 AI 生成、总结、提取待办，
支持 Web 辅助管理，
未来支持公开知识库、评论、CLI 和 AI Agent 调用。
```
