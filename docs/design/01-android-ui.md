# MKM Android UI 设计规范

## 1. 文档目标

本文档定义 MKM Android 端 V1 所有页面的 UI 逻辑、状态流转和交互规范。

Android 端是 MKM 的主产品端，V1 的所有核心功能都需要在 Android 上完整可用。

---

## 2. 整体导航结构

## 2.1 全局入口

```text
OnboardingActivity（首次启动 / 知识库初始化）
    └── MainActivity（主页）
        ├── MarkdownFragment     Tab 1 Markdown
        ├── AiAssistantFragment  Tab 2 AI（中间）
        └── ProfileFragment      Tab 3 我的
```

待办不作为主 Tab，通过 Markdown Toolbar 的日历入口进入。

```text
MarkdownFragment → 日历弹窗 → TodoActivity / TodoFragment
```

---

## 2.2 页面跳转关系

```text
OnboardingActivity
    → MainActivity

MainActivity
    ├── MarkdownFragment
    │       ├── DocumentDetailActivity
    │       ├── DocumentEditActivity
    │       ├── TodoActivity
    │       └── RightDrawer（知识库管理）
    │
    ├── AiAssistantFragment
    │       ├── AiProviderSettingsActivity
    │       └── AiConversationDetail
    │
    └── ProfileFragment
            ├── LoginActivity
            ├── AiProviderSettingsActivity
            ├── SyncSettingsActivity
            ├── ThemeSettingsActivity
            └── MarkdownRenderSettingsActivity
```

---

## 2.3 底部导航

底部导航使用悬浮 3 Tab：

```text
Markdown    AI    我的
```

设计要求：

```text
悬浮胶囊/卡片样式
左右留边距
不贴底实心栏
中间 AI 可视觉强调
支持深色/浅色主题
```

---

# 3. OnboardingActivity

## 3.1 页面目标

引导用户创建或打开第一个知识库。

## 3.2 UI 结构

```text
[MKM Logo / 名称]
面向程序员的 Markdown 知识库

[创建 App 内知识库]
[打开系统文件夹知识库]

[登录同步已有数据]（次要入口）
```

## 3.3 交互

| 操作 | 行为 |
|---|---|
| 创建 App 内知识库 | 输入知识库名称，创建在 App 私有目录 |
| 打开系统文件夹知识库 | 调用 SAF 选择目录，保存持久权限 |
| 登录同步已有数据 | 跳转 LoginActivity |

---

# 4. MarkdownFragment

## 4.1 页面目标

展示当前知识库内的 Markdown 文件夹树和文档列表，是 App 的主入口。

## 4.2 页面结构

```text
Top Toolbar
├── 用户头像
├── 搜索
├── 日历
└── 更多

Content
├── 当前知识库名称
├── 文件夹树
├── 卡片/列表视图切换
└── 文档列表 / 空状态

Floating Bottom Navigation
```

## 4.3 Toolbar 行为

| 图标 | 行为 |
|---|---|
| 用户头像 | 跳转 Profile 或显示登录入口 |
| 搜索 | 展开搜索输入，搜索文件名/标签/正文 |
| 日历 | 打开日历弹窗，弹窗内可进入待办页 |
| 更多 | 新建文件、新建文件夹、导入、视图切换、排序 |

## 4.4 右滑抽屉

```text
用户基本信息
当前知识库信息
新建知识库
打开知识库
切换知识库
知识库设置
```

## 4.5 状态

| 状态 | UI |
|---|---|
| Loading | 骨架屏 |
| Empty | 引导新建 Markdown 或导入文件 |
| Content | 文件夹树 + 文档列表 |
| Search | 搜索结果列表 |
| Offline | 本地模式提示，不影响使用 |
| Syncing | 文档同步小状态 |
| Conflict | 冲突标记，进入同步设置处理 |
| Error | Snackbar |

---

# 5. 文件夹树与文档列表

## 5.1 文件夹树

```text
知识库根目录
├── Android/
│   ├── Room.md
│   └── Retrofit.md
├── Backend/
│   └── Spring Security.md
└── README.md
```

交互：

```text
点击文件夹：展开/折叠
长按文件夹：重命名 / 移动 / 复制 / 删除
点击文档：进入详情
长按文档：重命名 / 移动 / 复制 / 删除 / 分享
```

## 5.2 卡片/列表视图

| 视图 | 内容 |
|---|---|
| 卡片 | 标题、摘要、标签、更新时间、同步状态 |
| 列表 | 文件名、路径、更新时间、同步状态 |

默认卡片视图。

---

# 6. DocumentDetailActivity

## 6.1 页面目标

展示 Markdown 渲染结果，并提供编辑、同步、AI 总结和附件查看能力。

## 6.2 页面结构

```text
Toolbar
├── 返回
├── 标题
├── 编辑/预览切换
└── 更多

Content
├── Markdown 渲染内容
├── 图片 / 附件展示
└── 同步状态
```

## 6.3 更多菜单

```text
编辑
AI 总结
开启/关闭云端同步
移动
复制
导出
分享
删除
```

---

# 7. DocumentEditActivity

## 7.1 页面目标

提供 Markdown 源码编辑体验，带快捷工具栏。

## 7.2 UI 结构

```text
标题输入
文件名输入
标签输入
Markdown 编辑区
Markdown 快捷工具栏
保存按钮
预览切换
```

快捷工具栏：

```text
H1 / H2
加粗
斜体
列表
任务列表
引用
代码块
链接
图片
```

## 7.3 退出保护

```text
有未保存内容 → 退出时弹窗确认
保存失败 → 保留编辑内容，不清空
```

---

# 8. TodoActivity / TodoFragment

## 8.1 入口

```text
Markdown Toolbar → 日历图标 → 日历弹窗 → 查看全部待办
```

## 8.2 页面结构

```text
日期筛选 / 今天
待办列表
新建待办 FAB
```

Todo 卡片字段：

```text
标题
备注
日期
优先级
完成状态
关联文档
```

## 8.3 交互

```text
点击圆圈：完成/取消完成
点击卡片：编辑待办
点击关联文档：打开文档详情
长按：删除
```

---

# 9. AiAssistantFragment

## 9.1 页面目标

提供用户自带 API Key 的 AI 多会话助手。

## 9.2 状态

| 状态 | UI |
|---|---|
| 未登录 | 显示登录引导 |
| 未配置 Provider | 显示配置 AI Key 引导 |
| 空会话 | 显示新建会话按钮和建议问题 |
| 对话中 | 消息列表 + 输入框 |
| 加载中 | AI 回复 loading |
| 错误 | Snackbar + 重试 |

## 9.3 UI 结构

```text
Top Bar
├── 会话列表入口
├── 当前会话标题
└── 新建会话

Message List
├── user message
└── assistant message（Markdown 渲染）

Input Bar
├── 输入框
└── 发送按钮
```

## 9.4 会话管理

```text
新建会话
切换会话
删除会话
云端同步历史
```

---

# 10. ProfileFragment

## 10.1 页面目标

提供完整设置中心。

## 10.2 UI 分组

```text
账号
├── 用户信息
├── 修改昵称 / 头像
├── 退出登录
└── 账号注销

AI
└── AI Key 配置

同步
├── 同步状态总览
├── 全局同步开关
├── 仅 Wi-Fi 同步
├── 冲突列表
├── 失败重试
└── 同步日志

外观
├── 浅色 / 深色 / 跟随系统
└── 自定义主色调

Markdown 渲染
├── 字体大小
├── 字体选择
├── 代码块主题
├── 行间距
├── 语法高亮开关
└── 图片加载策略

其他
├── 清理缓存
├── 通知设置
└── 关于
```

---

# 11. 同步冲突弹窗

触发条件：本地和云端同一文档都有修改。

UI：

```text
检测到同步冲突

本地版本：更新时间 xxx
云端版本：更新时间 xxx

[保留本地版本]
[保留云端版本]
[稍后处理]
```

V1 不做自动合并。

---

# 12. 删除与回收站

| 知识库类型 | 删除行为 |
|---|---|
| App 内知识库 | 删除进入 App 内回收站，可恢复/彻底删除 |
| 系统文件夹知识库 | 删除前强提示风险，不提供 App 回收站兜底 |

---

# 13. 空状态文案规范

| 场景 | 文案 |
|---|---|
| 无知识库 | 创建或打开一个知识库开始使用 |
| 无文档 | 新建第一篇 Markdown 文档 |
| 搜索无结果 | 没有找到相关文档 |
| 未登录 AI | 登录后使用 AI 助手 |
| 未配置 AI | 配置 OpenAI 兼容 API Key 后使用 AI |
| 无待办 | 暂无待办 |

---

# 14. Android UI 验收标准

```text
启动后可创建 App 内知识库
可打开系统文件夹知识库
主页为悬浮底部 3 Tab
Markdown 页 Toolbar 包含头像、搜索、日历、更多
右滑可打开知识库抽屉
文件夹树可展开/折叠
文档可卡片/列表切换
编辑器包含 Markdown 快捷工具栏
日历入口可进入待办页
AI 页支持未登录/未配置/对话中状态
我的页包含完整设置分组
同步冲突弹窗可选择保留本地或云端
```
