# MKM Android 端规范说明

## 1. 文档目标

本文档定义 MKM Android 端的产品范围、技术约束、页面结构、数据流、状态管理和验收标准。

Android 是 MKM V1 的主产品端，必须优先保证完整体验。

---

# 2. Android 端定位

Android 端定位：

```text
MKM 的主体验端
负责本地仓库初始化、Markdown 阅读/编辑、Todo 管理、AI 使用、公开知识库浏览和个人设置
```

V1 设计原则：

```text
本地优先
仓库优先
登录可选
离线可用
AI 配置可选
界面简洁，适合手机阅读 Markdown
```

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
```

---

## 3.2 避免事项

```text
V1 不使用 Jetpack Compose 作为主 UI
不要在 Activity / Fragment 中直接写复杂业务逻辑
不要在前端长期保存完整 API Key
不要将 Token、API Key 打入日志
不要让网络失败清空本地数据
```

---

# 4. Android 项目结构建议

```text
android/app/src/main/java/com/mkm/android/
├── data/
│   ├── local/              Room、DataStore、本地仓库访问
│   ├── remote/             Retrofit API
│   └── repository/         Repository 层
├── model/                  UI/Domain 数据模型
├── ui/
│   ├── onboarding/         首次启动、仓库初始化
│   ├── auth/               登录注册
│   ├── markdown/           文档列表、目录、搜索
│   ├── document/           文档详情、编辑、预览
│   ├── todo/               待办列表和详情
│   ├── ai/                 AI 助手、Provider 设置
│   ├── knowledge/          公开知识库
│   └── profile/            我的、设置
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
├── TodoFragment
├── AiAssistantFragment
├── KnowledgeFragment
└── ProfileFragment
DocumentDetailActivity
AiProviderSettingsActivity
```

---

## 5.2 启动页规范

App 启动时不直接进入 LoginActivity。

启动判断：

```text
已完成仓库初始化 → MainActivity
未完成仓库初始化 → OnboardingActivity
```

登录只在以下场景出现：

```text
开启云同步
从账号恢复仓库
未登录时评论/点赞
我的页面主动登录
```

---

# 6. Onboarding 规范

## 6.1 首次启动流程

```text
欢迎页
    │
    ├─ 创建新仓库
    │       ├─ 开启云同步 → 登录/注册 → 设置仓库信息 → 主页
    │       └─ 跳过同步 → 设置仓库信息 → 主页
    │
    └─ 使用现有仓库
            ├─ 打开本地仓库 → 选择目录 → 校验 → 主页
            └─ 从账号恢复 → 登录/注册 → 选择云端仓库 → 主页
```

---

## 6.2 存储位置

Android 端必须支持两种仓库位置：

| 位置 | 说明 | 技术建议 |
|---|---|---|
| 设备存储 | 其他应用可以访问 | Storage Access Framework，`ACTION_OPEN_DOCUMENT_TREE` |
| 应用存储 | App 私有目录，卸载后删除 | `context.filesDir` |

---

# 7. 本地数据规范

## 7.1 Room 使用范围

Room 用于本地缓存和离线展示：

```text
DocumentEntity
TodoEntity
AiConversationEntity
AiMessageEntity
RepositoryEntity
SyncStateEntity
```

---

## 7.2 DataStore 使用范围

DataStore 用于轻量配置：

```text
jwt_token
current_repository_id
theme_mode
language
last_sync_time
has_completed_onboarding
```

---

## 7.3 本地仓库文件访问

设备存储模式下，必须通过 SAF 访问用户选择目录。

要求：

```text
保存 persistableUriPermission
处理目录权限失效
处理文件被其他 App 修改
处理文件删除或重命名
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
```

页面原则：

```text
有缓存先展示缓存
网络失败不清空已有数据
操作中按钮 disabled
错误用 Snackbar
删除类操作支持确认或撤销
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
图片（本地 assets）
```

---

## 10.2 编辑

编辑器使用 XML + EditText 实现。

必须支持：

```text
标题输入
标签输入
Markdown 正文输入
保存按钮
未保存退出确认
Markdown 辅助工具栏
```

---

# 11. Todo 规范

Todo 端必须支持：

```text
未完成 / 已完成 Tab
新建待办 BottomSheet
优先级：低 / 中 / 高
截止时间
备注
来源文档跳转
完成动画
滑动删除 + 撤销
```

---

# 12. AI 规范

## 12.1 AI 助手

AI 助手页必须支持：

```text
未配置 Provider 空状态
空对话快捷操作
多轮对话
历史对话
模型切换
AI 消息 Markdown 渲染
复制 / 保存文档 / 提取待办
```

---

## 12.2 Provider 设置

Provider 设置页必须支持：

```text
查看已配置模型
添加模型
选择预设
自定义 Base URL
填写 API Key
测试连接
保存配置
删除配置
脱敏显示 API Key
```

---

# 13. Knowledge 规范

公开知识库页必须支持：

```text
公开文档列表
搜索
标签筛选
只读文档详情
点赞
评论
AI 总结
```

未登录用户：

```text
可浏览公开文档
可查看评论
不可点赞
不可评论
```

---

# 14. Profile 规范

我的页面必须分组：

```text
账号
仓库
AI
外观
其他
```

未登录时：

```text
显示登录入口
仓库信息正常展示
本地功能正常可用
```

已登录时：

```text
显示用户信息
显示同步状态
可退出登录
```

---

# 15. 安全规范

Android 端必须遵守：

```text
Token 使用 DataStore 保存
不要保存完整 API Key
不要日志打印 Token / API Key
公开文档操作需明确用户确认
删除文档需确认
导出仓库需确认
```

---

# 16. Android 验收标准

V1 Android 端必须完成以下闭环：

```text
首次启动 → 创建本地仓库 → 进入首页
新建 Markdown → 编辑 → 保存 → 预览
搜索文档 → 切换列表/宫格 → 筛选文档
创建 Todo → 设置优先级 → 标记完成 → 删除撤销
配置 DeepSeek API Key → AI 提问 → 保存为文档
打开公开文档 → 评论 → 点赞
我的页面 → 查看仓库 → 开启同步 → 登录
```
