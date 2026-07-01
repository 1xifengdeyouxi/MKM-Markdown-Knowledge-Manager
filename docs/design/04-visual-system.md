# MKM Android 视觉规范系统

## 1. 文档目标

本文档定义 MKM Android 端的视觉规范，包括颜色系统、字体层级、间距、圆角、阴影、组件样式，确保 App 视觉一致性。

---

# 2. 颜色系统

## 2.1 主题色

MKM 使用 Material Design 3 颜色系统。

| 颜色名 | 用途 | 浅色值 | 深色值 |
|---|---|---|---|
| Primary | 主色：按钮、图标强调、选中状态 | `#3F51B5` Indigo | `#7986CB` |
| On Primary | Primary 上的文字/图标 | `#FFFFFF` | `#000000` |
| Secondary | 次要强调：AI 相关元素、标签 | `#009688` Teal | `#4DB6AC` |
| On Secondary | Secondary 上的文字/图标 | `#FFFFFF` | `#000000` |
| Surface | 卡片、弹窗、页面背景 | `#FFFFFF` | `#1C1B1F` |
| On Surface | Surface 上的主要文字 | `#212121` | `#E6E1E5` |
| Surface Variant | 分组背景、输入框背景 | `#F5F5F5` | `#2C2B30` |
| Outline | 分割线、输入框边框 | `#BDBDBD` | `#938F99` |
| Error | 错误状态、高优先级标签 | `#B00020` | `#CF6679` |

---

## 2.2 语义颜色

| 颜色 | 用途 | 值 |
|---|---|---|
| Success | 同步成功、连接成功 | `#4CAF50` |
| Warning | 待办即将到期、低电量等 | `#FF9800` |
| Info | 提示信息、普通状态 | `#2196F3` |

---

## 2.3 待办优先级颜色

| 优先级 | 颜色 | 值 |
|---|---|---|
| 🔴 高 | 红色 | `#F44336` |
| 🟡 中 | 橙黄 | `#FF9800` |
| 🟢 低 | 绿色 | `#4CAF50` |

---

## 2.4 Markdown 渲染颜色

在 Markwon 中应用的自定义样式：

| 元素 | 颜色/样式 |
|---|---|
| 一级标题 H1 | Primary 色 `#3F51B5`，字重 700 |
| 二级标题 H2 | On Surface `#212121`，字重 600 |
| 代码块背景 | `#F5F5F5`（浅色）/ `#2C2B30`（深色） |
| 代码块文字 | `#C62828` Monospace |
| 行内代码 | 背景 `#EEEEEE`，文字 `#C62828` |
| 链接 | Primary 色，下划线 |
| 引用块左边框 | Secondary 色 `#009688` |
| 表格边框 | Outline `#BDBDBD` |

---

# 3. 字体系统

## 3.1 字体选择

V1 使用系统字体（无需自定义字体文件）：

```text
中文：系统默认（Android 通常为思源黑体 / Noto Sans SC）
英文 / 数字：Roboto（Android 系统默认）
代码：Monospace（系统等宽字体）
```

---

## 3.2 字体层级

基于 Material Design 3 Type Scale：

| 层级 | 用途 | 字号 | 字重 | 行高 |
|---|---|---|---|---|
| Display Large | 启动页大标题 | 57sp | 400 | 64sp |
| Headline Large | 页面主标题 | 32sp | 400 | 40sp |
| Headline Medium | 页面副标题 | 28sp | 400 | 36sp |
| Title Large | Toolbar 标题 | 22sp | 400 | 28sp |
| Title Medium | 卡片标题 | 16sp | 500 | 24sp |
| Title Small | 分组标题 | 14sp | 500 | 20sp |
| Body Large | 正文主要内容 | 16sp | 400 | 24sp |
| Body Medium | 正文次要内容、卡片副标题 | 14sp | 400 | 20sp |
| Body Small | 时间戳、标签文字 | 12sp | 400 | 16sp |
| Label Large | 按钮文字 | 14sp | 500 | 20sp |
| Label Small | 底部导航标签 | 11sp | 500 | 16sp |

---

## 3.3 常用场景字号

| 场景 | 字号 | 说明 |
|---|---|---|
| 文档列表标题 | 16sp Title Medium | 清晰可辨 |
| 文档时间/标签 | 12sp Body Small | 辅助信息 |
| Markdown H1 | 22sp | 渲染时 |
| Markdown H2 | 18sp | 渲染时 |
| Markdown H3 | 16sp | 渲染时 |
| Markdown 正文 | 15sp | 阅读体验 |
| Markdown 代码 | 13sp Monospace | 代码等宽 |
| 聊天用户消息 | 15sp | |
| 聊天 AI 消息 | 15sp | Markdown 渲染 |
| BottomNav 标签 | 11sp | |

---

# 4. 间距系统

## 4.1 基础间距单位

MKM 使用 8dp 为基础间距单位：

```text
4dp   — 极小间距：图标与文字之间
8dp   — 小间距：列表项内部间距
12dp  — 中小间距：卡片内边距
16dp  — 标准间距：页面边距、卡片外边距
24dp  — 大间距：页面区域间距
32dp  — 超大间距：页面顶部/底部留白
```

---

## 4.2 常用间距规范

| 场景 | 间距 |
|---|---|
| 页面水平边距 | 16dp |
| 卡片内边距 | 12dp horizontal + 10dp vertical |
| 卡片之间外边距 | 8dp bottom |
| Toolbar 高度 | 56dp（Material 标准） |
| BottomNav 高度 | 56dp |
| FAB 距底部边距 | 16dp |
| FAB 距右边距 | 16dp |
| 列表项内部竖向间距 | 8dp |
| 分割线 | 1dp，Outline 颜色 |
| 输入框内边距 | 16dp horizontal + 12dp vertical |
| BottomSheet 内边距 | 16dp horizontal + 16dp top |
| Dialog 内边距 | 24dp horizontal + 20dp vertical |

---

## 4.3 Markdown 渲染间距

| 元素 | 间距规范 |
|---|---|
| 段落间距 | 12dp margin bottom |
| 标题上方间距 | 16dp margin top |
| 标题下方间距 | 8dp margin bottom |
| 代码块内边距 | 12dp |
| 代码块圆角 | 8dp |
| 引用块左边框宽 | 4dp |
| 引用块左内边距 | 12dp |
| 列表项缩进 | 16dp per level |
| 表格单元格内边距 | 8dp horizontal + 6dp vertical |

---

# 5. 圆角系统

## 5.1 圆角层级

基于 Material Design 3 Shape System：

| 组件 | 圆角 |
|---|---|
| 卡片（文档/待办） | 12dp |
| 按钮（普通） | 20dp（全圆角 / pill） |
| 输入框 | 12dp（填充样式） |
| BottomSheet | 顶部 28dp，底部 0dp |
| Dialog | 28dp |
| 标签 Chip | 8dp |
| Snackbar | 4dp |
| FAB | 16dp |
| 代码块 | 8dp |
| 消息气泡（用户） | 16dp，右下角 4dp |
| 消息气泡（AI） | 16dp，左下角 4dp |
| 快捷操作卡片 | 12dp |
| 头像 | 圆形 |

---

# 6. 阴影系统

## 6.1 阴影层级

| 层级 | elevation | 使用场景 |
|---|---|---|
| 0 | 0dp | 背景、列表项 |
| 1 | 1dp | 卡片（轻微浮起） |
| 2 | 2dp | 标准卡片 |
| 4 | 4dp | Toolbar、BottomNav |
| 6 | 6dp | FAB |
| 8 | 8dp | BottomSheet |
| 12 | 12dp | Dialog |

深色模式下降低阴影依赖，通过颜色区分层级而非阴影。

---

# 7. 组件样式规范

## 7.1 按钮

### 主按钮（FilledButton）

```text
背景色：Primary
文字色：On Primary
高度：48dp
水平内边距：24dp
圆角：20dp（pill）
字号：14sp Label Large
```

使用场景：页面主操作（登录、保存、创建仓库）。

---

### 次要按钮（OutlinedButton）

```text
背景色：透明
边框：1dp Outline 色
文字色：Primary
高度：48dp
水平内边距：24dp
圆角：20dp
```

使用场景：取消、次要操作。

---

### 文字按钮（TextButton）

```text
背景色：透明
文字色：Primary
高度：40dp
```

使用场景：弹窗内的链接式操作，如"暂不同步，继续本地使用"。

---

### FAB

```text
背景色：Primary Container
图标色：On Primary Container
尺寸：56dp × 56dp
圆角：16dp
elevation：6dp
```

---

## 7.2 输入框（TextInputLayout）

统一使用 FilledTextField 风格：

```text
背景：Surface Variant `#F5F5F5`
圆角：12dp 上方，0dp 下方（或统一 12dp）
文字颜色：On Surface
提示文字颜色：On Surface 60% 透明
激活边框：Primary
错误边框：Error
```

---

## 7.3 卡片（MaterialCardView）

```text
背景：Surface
圆角：12dp
阴影：2dp elevation
边框：可选 1dp Outline（轻量卡片）
内边距：12dp × 10dp
```

---

## 7.4 标签 Chip

标签（Tags）使用 Chip 样式：

```text
背景：Secondary Container（Teal 浅色）
文字：On Secondary Container
圆角：8dp
高度：32dp
水平内边距：12dp
```

---

## 7.5 消息气泡

### 用户消息

```text
背景：Primary Container
文字：On Primary Container
圆角：16dp，右下角 4dp
最大宽度：屏幕宽度 75%
内边距：12dp × 8dp
```

### AI 消息

```text
背景：Surface Variant
文字：On Surface
圆角：16dp，左下角 4dp
最大宽度：屏幕宽度 85%（AI 消息通常较长）
内边距：12dp × 8dp
```

---

## 7.6 BottomNavigationView

```text
高度：56dp
图标尺寸：24dp
文字：11sp Label Small
选中图标颜色：Primary
未选中图标颜色：On Surface Variant（60%）
背景色：Surface
顶部分割线：1dp Outline
```

AI 中间 Tab 可视觉突出：

```text
AI 图标使用 Primary 色圆形背景（filled icon indicator）
```

---

## 7.7 Snackbar

```text
背景：On Surface（深色，与页面形成对比）
文字：Surface（白色）
Action 文字：Secondary（Teal）
圆角：4dp
位置：页面底部，距离 BottomNav 8dp
最大宽度：屏幕宽度 - 32dp
```

---

## 7.8 BottomSheet

```text
背景：Surface
顶部圆角：28dp
顶部 DragHandle：宽 32dp，高 4dp，颜色 Outline，居中，距顶 8dp
内容内边距：16dp horizontal，从 DragHandle 下方 16dp 开始
```

---

## 7.9 Dialog

```text
背景：Surface
圆角：28dp
标题字号：Title Large 22sp
内容字号：Body Large 16sp
内边距：24dp horizontal，20dp vertical
按钮区域：右对齐，TextButton 风格
```

---

# 8. 图标规范

## 8.1 图标库

使用 Material Symbols（Google 官方 Material Icons）：

```text
material-symbols-outlined（线条风格，V1 推荐）
```

---

## 8.2 图标尺寸

| 场景 | 尺寸 |
|---|---|
| Toolbar 图标 | 24dp |
| BottomNav 图标 | 24dp |
| 卡片内图标 | 20dp |
| 列表项前置图标 | 24dp |
| 空状态插图（可用系统图标） | 64dp |
| FAB 图标 | 24dp |

---

## 8.3 底部导航图标建议

| Tab | Material Icon | 含义 |
|---|---|---|
| 文档 | `description` | 文档/文件 |
| 待办 | `checklist` | 清单 |
| AI | `smart_toy` | AI 机器人 |
| 知识库 | `auto_stories` | 知识库/书 |
| 我的 | `person` | 用户 |

---

# 9. 深色模式规范

## 9.1 颜色反转原则

深色模式不是简单地把白色改黑色，而是使用对应的 Dark Token：

```text
Surface → #1C1B1F（深灰背景）
Surface Variant → #2C2B30（略浅背景）
Primary → #7986CB（变浅，保证对比度）
On Surface → #E6E1E5（浅灰文字）
Outline → #938F99（暗化）
```

---

## 9.2 代码块深色

深色模式下代码块使用深色背景：

```text
背景：#2D2D2D
文字颜色参考 VS Code Dark+ 主题
关键字：#569CD6 蓝
字符串：#CE9178 橙
注释：#6A9955 绿
数字：#B5CEA8
```

---

## 9.3 Markdown 渲染深色适配

| 元素 | 深色值 |
|---|---|
| H1 颜色 | `#7986CB`（浅靛蓝） |
| 代码块背景 | `#2C2B30` |
| 引用块左边框 | `#4DB6AC` |
| 链接颜色 | `#80CBC4` |

---

# 10. 动效规范

## 10.1 页面切换

使用 Material Motion 标准：

| 切换场景 | 动效 |
|---|---|
| BottomNav Tab 切换 | Fade Through（淡入淡出） |
| Activity 进入 | Shared Axis Z（缩放淡入） |
| DocumentDetail 打开 | Container Transform（卡片展开） |
| BottomSheet 弹出 | 从底部滑入，200ms ease out |
| Dialog 弹出 | 缩放 + 淡入，200ms |

---

## 10.2 列表动效

| 场景 | 动效 |
|---|---|
| 待办完成 | 划线动画（300ms）+ 渐出（200ms）+ 移入已完成 Tab |
| 待办删除（滑动） | 右滑出红色删除区域 |
| 新建待办插入 | 列表顶部渐入（200ms） |
| 文档卡片加载 | Skeleton（骨架屏）→ 内容渐入 |

---

## 10.3 加载动效

| 场景 | 组件 |
|---|---|
| 页面初始加载 | 顶部 LinearProgressIndicator |
| AI 回复等待 | 三点 Typing 动画（每点 delay 200ms） |
| 按钮操作中 | CircularProgressIndicator（小，替换图标） |
| 同步中 | Toolbar 旁旋转图标 |

---

# 11. 空状态设计

每个列表页面需要有统一风格的空状态：

```text
┌──────────────────────────┐
│                          │
│   [插图 / 大图标 64dp]   │
│                          │
│       标题文字            │
│       副标题/说明         │
│                          │
│      [行动按钮（可选）]   │
│                          │
└──────────────────────────┘
```

空状态说明文案建议：

| 页面 | 标题 | 副文字 | 按钮 |
|---|---|---|---|
| 文档列表空 | 还没有文档 | 新建你的第一篇 Markdown 文档 | 新建文档 |
| 待办空（未完成） | 没有待办事项 | 点击 + 添加你的第一条待办 | — |
| 待办空（已完成） | 还没有完成的待办 | 去完成几条任务吧 | — |
| AI 未配置 | 还没有 AI 模型 | 添加 DeepSeek 等模型开始使用 | 去配置 |
| 知识库空 | 暂无公开文档 | 换个关键词试试 | — |

---

# 12. 当前结论

MKM Android V1 视觉规范：

```text
颜色：Material Design 3，主色 Indigo #3F51B5，次色 Teal #009688
字体：系统字体，16sp 正文，12sp 辅助
间距：8dp 为单位，16dp 标准页面边距
圆角：12dp 卡片，28dp BottomSheet，20dp 按钮
阴影：2dp 卡片，6dp FAB，8dp BottomSheet
深色模式：跟随系统，完整 Dark Token 映射
动效：Material Motion，200-300ms 标准时长
空状态：图标 + 文案 + 可选按钮，统一风格
```
