# MKM Android 端规范说明

## 1. 文档目标

本文档定义 MKM Android 端的产品范围、技术约束、页面结构、数据流、状态管理和验收标准。

Android 是 MKM V1 的主产品端，必须优先保证完整体验。

---

# 2. Android 端定位

Android 端定位：

```text
MKM 的主体验端
负责知识库初始化、Markdown 阅读/编辑、文件夹管理、Todo 管理、AI 使用和个人设置
```

V1 设计原则：

```text
本地优先
知识库优先
登录可选
离线可用
每篇文档独立控制云端同步
AI 必须登录并配置 API Key 后使用
界面简洁，适合手机阅读 Markdown
```

V1 不做社区、评论、点赞、公开知识库浏览。

---

# 3. 技术栈规范

## 3.1 必选技术

```text
Kotlin
XML Layout
Material Components
Navigation Component
ViewModel + LiveData / StateFlow
Retrofit + OkHttp
Room
DataStore
Markwon
Storage Access Framework
```

---

## 3.2 开发环境建议

| 项目 | 推荐版本 / 工具 | 说明 |
|---|---|---|
| JDK | **17 LTS** | 与 Backend 共用，Gradle JDK 也选 17 |
| Android Studio | **Meerkat 2024.3.1** 或更新 | 主开发 IDE |
| Android Gradle Plugin | **8.3.2** | 已在根 `build.gradle.kts` 锁定 |
| Kotlin | **1.9.25** | 与 KSP 版本匹配 |
| KSP | **1.9.25-1.0.20** | Room 注解处理器 |
| compileSdk / targetSdk | **34** | 已安装 Android 14 SDK |
| minSdk | **26** | Android 8.0+ |
| 模拟器 | Pixel 7 / API 34 / arm64-v8a | Apple Silicon Mac 使用 arm64 镜像 |

详细安装说明见：`docs/deployment/00-dev-environment.md`。

---

## 3.3 核心依赖版本

| 依赖 | 版本 |
|---|---|
| Material Components | 1.12.0 |
| Navigation Fragment/UI KTX | 2.7.7 |
| Lifecycle ViewModel / LiveData KTX | 2.8.4 |
| Coroutines Android | 1.8.1 |
| Retrofit | 2.11.0 |
| OkHttp | 4.12.0 |
| Room | 2.6.1 |
| DataStore Preferences | 1.1.1 |
| Markwon | 4.6.2 |

---

## 3.4 避免事项

```text
V1 不使用 Jetpack Compose 作为主 UI
不要在 Activity / Fragment 中直接写复杂业务逻辑
不要在前端长期保存完整 API Key
不要将 Token、API Key 打入日志
不要让网络失败清空本地数据
不要把登录作为使用 App 的前置条件
```

---

# 4. Android 项目结构建议

```text
android/app/src/main/java/com/mkm/android/
├── data/
│   ├── local/              Room、DataStore、本地知识库访问
│   ├── remote/             Retrofit API
│   └── repository/         Repository 层
├── model/                  UI/Domain 数据模型
├── ui/
│   ├── onboarding/         首次启动、知识库初始化
│   ├── auth/               登录注册
│   ├── markdown/           文件夹树、文档列表、搜索、标签
│   ├── document/           文档详情、编辑、预览
│   ├── todo/               待办列表和详情（日历入口进入）
│   ├── ai/                 AI 助手、多会话、Provider 设置
│   └── profile/            我的、同步、主题、渲染偏好
└── util/                   通用工具
```

---

# 5. 页面规范

## 5.1 页面清单

```text
OnboardingActivity
LoginActivity
MainActivity
├── MarkdownFragment
├── AiAssistantFragment
└── ProfileFragment
TodoActivity / TodoFragment
DocumentDetailActivity
DocumentEditActivity
AiProviderSettingsActivity
SyncSettingsActivity
MarkdownRenderSettingsActivity
ThemeSettingsActivity
```

---

## 5.2 主导航规范

MainActivity 使用悬浮底部 3 Tab：

```text
Markdown    AI    我的
```

要求：

```text
底部导航为悬浮胶囊/卡片样式
不使用 5 Tab
AI 位于中间，可视觉强调
Markdown 首页支持右滑抽屉
```

---

## 5.3 Markdown Toolbar

```text
用户头像    搜索    日历    更多
```

| 操作 | 行为 |
|---|---|
| 用户头像 | 跳转我的页或展示账号入口 |
| 搜索 | 搜索文件名、标签、正文内容 |
| 日历 | 展示日历弹窗，弹窗内可跳转待办页 |
| 更多 | 展示新建、导入、排序、视图切换等操作 |

---

## 5.4 右滑抽屉

```text
用户基本信息
──────────────
当前知识库信息
新建知识库
打开知识库（App 内 / 系统文件夹）
切换知识库
知识库设置
```

---

# 6. Onboarding 规范

## 6.1 首次启动流程

```text
欢迎页
    │
    ├─ 创建 App 内知识库
    │       └─ 设置知识库名称 → 主页
    │
    └─ 打开系统文件夹知识库
            └─ 选择目录 → 校验权限 → 主页
```

登录只在以下场景出现：

```text
开启云同步
从账号恢复同步数据
使用 AI
我的页面主动登录
```

---

## 6.2 存储位置

Android 端必须支持两种知识库位置：

| 位置 | 说明 | 技术建议 |
|---|---|---|
| App 内知识库 | App 私有目录，稳定安全 | `context.filesDir` |
| 系统文件夹知识库 | 用户选择目录，其他应用可访问 | Storage Access Framework，`ACTION_OPEN_DOCUMENT_TREE` |

---

# 7. 本地数据规范

## 7.1 Room 使用范围

Room 用于本地缓存、离线展示和同步状态管理：

```text
DocumentEntity
FolderEntity
TagEntity
AttachmentEntity
TodoEntity
AiConversationEntity
AiMessageEntity
RepositoryEntity
SyncStateEntity
SyncLogEntity
```

---

## 7.2 DataStore 使用范围

DataStore 用于轻量配置：

```text
jwt_token
current_repository_id
theme_mode
custom_theme_color
markdown_font_size
markdown_font_family
markdown_line_spacing
code_highlight_enabled
image_load_strategy
last_sync_time
has_completed_onboarding
```

---

## 7.3 系统文件夹访问

系统文件夹知识库必须通过 SAF 访问用户选择目录。

要求：

```text
保存 persistableUriPermission
处理目录权限失效
处理文件被其他 App 修改
处理文件删除或重命名
删除前强提示风险
```

---

# 8. 网络请求规范

## 8.1 Retrofit

所有后端 API 通过 Retrofit 调用。

要求：

```text
统一 Base URL
统一 Bearer Token 注入
统一 401 处理
统一错误包装
禁止日志打印 Authorization 和 API Key
```

---

## 8.2 401 处理

```text
收到 401
    │
    └─ 清除 Token
        └─ Snackbar "登录已过期 [重新登录]"
            ├─ 用户点击重新登录 → LoginActivity
            └─ 用户忽略 → App 降级本地模式继续使用
```

不得强制跳登录页。

---

# 9. UI 状态规范

每个页面至少处理以下状态：

```text
Loading
Content
Empty
Error
Offline / Local mode
Syncing
Conflict
```

页面原则：

```text
有缓存先展示缓存
网络失败不清空已有数据
操作中按钮 disabled
错误用 Snackbar
删除类操作支持确认或撤销
同步冲突必须让用户选择保留本地或云端
```

---

# 10. Markdown 规范

## 10.1 渲染

使用 Markwon 渲染 Markdown。

必须支持：

```text
标题
列表
任务列表
引用
代码块
行内代码
表格
链接
网络图片
相对路径本地图片
附件链接（PDF/ZIP/其他）
```

---

## 10.2 编辑

编辑器使用 XML + EditText 实现，底层仍然是 Markdown 源码。

必须支持：

```text
标题输入
标签输入
Markdown 正文输入
保存按钮
未保存退出确认
预览/编辑模式切换
Markdown 辅助工具栏（加粗、斜体、标题、列表、代码块、链接等）
```

---

## 10.3 文件操作

必须支持：

```text
新建文件
新建文件夹
重命名
删除
移动文件 / 文件夹
复制文件 / 文件夹
导入 .md 文件
导出 .md 文件
分享 .md 文件
```

删除策略：

```text
App 内知识库：进入 App 内回收站，可恢复/彻底删除
系统文件夹知识库：删除前强提示风险
```

---

# 11. 搜索与标签规范

## 11.1 搜索范围

```text
文件名
标签
正文内容
```

## 11.2 标签能力

```text
创建标签
给文档打标签
按标签筛选文档列表
```

---

# 12. 附件规范

## 12.1 附件类型

```text
图片：网络图片 + 相对路径本地图片预览
附件：PDF / ZIP / 其他文件作为附件显示和打开
```

## 12.2 同步限制

```text
同步 Markdown + 全部附件
单文件最大 10MB
超过 10MB 可保留本地引用，但提示暂不支持同步
```

## 12.3 引用策略

```text
本地 Markdown 原文保持相对路径
本地渲染时按知识库目录解析相对路径
云端/Web 渲染时由后端/API 把相对路径解析为附件下载地址
```

---

# 13. Todo 规范

Todo 不作为主 Tab，入口为：

```text
Markdown Toolbar → 日历弹窗 → 待办页
```

Todo 必须支持：

```text
标题
备注
日期
优先级：低 / 中 / 高
完成状态
关联 Markdown 文档
用户手动创建
AI 提取创建
完成 / 取消完成
删除
```

---

# 14. AI 规范

## 14.1 AI 助手

AI 助手页必须支持：

```text
未登录空状态
未配置 Provider 空状态
多会话管理（新建/切换/删除）
对话历史云端同步
多轮对话
AI 消息 Markdown 渲染
复制 / 提取待办 / 生成 Markdown
当前文档总结
```

---

## 14.2 Provider 设置

V1 只支持一个 OpenAI 兼容配置。

Provider 设置页必须支持：

```text
Base URL
API Key
Model 名称
测试连接
保存配置
删除配置
脱敏显示 API Key
```

不做多 Provider 模板和多模型切换。

---

# 15. Profile 规范

我的页面必须包含：

```text
账号：用户信息、修改昵称/头像、退出登录、账号注销
AI：AI Key 配置
同步：同步状态、全局同步开关、仅 Wi-Fi、冲突列表、失败重试、同步日志
外观：浅色/深色/跟随系统、自定义主色调
Markdown 渲染：字体大小、字体选择、代码块主题、行间距、语法高亮、图片加载策略
其他：清理缓存、通知设置、关于
```

未登录时：

```text
显示登录入口
本地知识库正常展示
本地功能正常可用
AI 和云同步显示登录引导
```

---

# 16. 同步规范

```text
每篇文档独立控制是否云端同步
开启同步的文档有网时自动后台上传
全局同步开关可暂停所有自动同步
支持仅 Wi-Fi 同步
冲突时弹窗让用户选择保留本地或云端
失败可重试
记录同步日志
```

---

# 17. 安全规范

Android 端必须遵守：

```text
Token 使用 DataStore 保存
不要保存完整 API Key
不要日志打印 Token / API Key
删除文档需确认
导出知识库需确认
系统文件夹知识库删除需强提示风险
```

---

# 18. Android 验收标准

V1 Android 端必须完成以下闭环：

```text
首次启动 → 创建 App 内知识库 → 进入 Markdown 首页
打开系统文件夹 → 展示文件夹树 → 打开 .md 文件
新建 Markdown → 编辑工具栏 → 保存 → 预览
搜索文件名/标签/正文 → 筛选文档
新建文件夹 → 移动文档 → 删除进回收站 → 恢复
配置 OpenAI-compatible API Key → AI 提问 → 切换历史会话
打开文档 → AI 总结 → 提取待办
Toolbar 日历 → 待办页 → 创建待办 → 标记完成
我的页面 → 同步设置 → 冲突选择保留本地/云端
主题设置 → 切换深色/浅色/自定义主色
```
