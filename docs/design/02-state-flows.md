# MKM 页面状态流转与导航规范

## 1. 文档目标

本文档定义 MKM Android 端的状态流转图和页面导航规范，补充 UI 设计文档中每个页面的状态机。

---

# 2. 全局启动状态机

```text
App 启动
    │
    ├─ 已完成知识库初始化（本地标记存在）
    │       │
    │       ├─ 无 Token（未登录）──→ MainActivity（本地模式）
    │       │
    │       └─ 有 Token ──→ Token 验证
    │                           │
    │                     ├─ 有效 ──→ MainActivity（已登录）
    │                     └─ 过期 ──→ 清除 Token → MainActivity（本地模式）
    │
    └─ 未完成知识库初始化
            └─ OnboardingActivity
```

原则：

```text
知识库初始化优先于登录
登录不是使用 App 的前置条件
Token 失效不能阻断本地功能
```

---

# 3. Onboarding 状态流

```text
OnboardingActivity
    │
    ├─ 创建 App 内知识库
    │       ├─ 输入知识库名称
    │       ├─ 创建私有目录
    │       └─ 保存 current_repository_id → MainActivity
    │
    ├─ 打开系统文件夹知识库
    │       ├─ 调用 ACTION_OPEN_DOCUMENT_TREE
    │       ├─ 获取 persistableUriPermission
    │       ├─ 扫描 .md 文件
    │       └─ 保存 repository → MainActivity
    │
    └─ 登录同步已有数据
            └─ LoginActivity
```

异常状态：

```text
目录权限被拒绝 → 回到选择页并提示
目录为空 → 允许进入，显示空知识库
扫描失败 → 提示错误，可重试
```

---

# 4. MainActivity 导航状态

```text
MainActivity
    │
    ├─ Markdown Tab
    ├─ AI Tab
    └─ 我的 Tab
```

底部导航状态：

```text
Markdown selected
AI selected
Profile selected
```

待办不是 Tab：

```text
Markdown Toolbar 日历 → Calendar Popup → TodoActivity
```

---

# 5. Markdown 页面状态机

```text
MarkdownFragment
    │
    ├─ Loading
    │     └─ 读取 Room / 文件系统
    │
    ├─ Empty
    │     ├─ 新建 Markdown
    │     ├─ 新建文件夹
    │     └─ 导入 .md
    │
    ├─ Content
    │     ├─ 文件夹树
    │     ├─ 卡片视图
    │     └─ 列表视图
    │
    ├─ Search
    │     └─ 文件名 + 标签 + 正文搜索
    │
    ├─ Syncing
    │     └─ 展示同步状态，不阻塞本地编辑
    │
    ├─ Conflict
    │     └─ 提示进入冲突处理
    │
    └─ Error
          └─ Snackbar + 重试
```

---

# 6. 文件操作状态流

## 6.1 新建文件

```text
点击新建文件
    │
    ├─ 当前有选中文件夹 → 默认 folderPath = 当前文件夹
    └─ 无选中文件夹 → 默认 folderPath = /
            │
            └─ DocumentEditActivity → 保存 → 返回 MarkdownFragment
```

---

## 6.2 删除文件

```text
点击删除
    │
    ├─ App 内知识库
    │       ├─ 确认删除
    │       └─ 移入 App 回收站
    │
    └─ 系统文件夹知识库
            ├─ 强提示风险
            ├─ 用户确认
            └─ 删除真实文件
```

---

## 6.3 移动 / 复制

```text
长按文件或文件夹
    │
    ├─ 选择移动 / 复制
    ├─ 选择目标文件夹
    └─ 执行操作 → 刷新文件夹树
```

---

# 7. Document 详情与编辑状态流

## 7.1 查看文档

```text
DocumentDetailActivity
    │
    ├─ Loading
    ├─ Render Markdown
    ├─ 展示图片 / 附件
    └─ 显示同步状态
```

## 7.2 编辑文档

```text
点击编辑
    │
    └─ DocumentEditActivity
            │
            ├─ 修改标题 / 文件名 / 标签 / 正文
            ├─ 使用 Markdown 工具栏
            ├─ 保存
            │     ├─ 本地保存成功 → 返回详情
            │     └─ 本地保存失败 → 保留内容并提示
            └─ 退出
                  ├─ 无修改 → 返回
                  └─ 有未保存修改 → 弹窗确认
```

---

# 8. 同步状态流

## 8.1 文档同步状态

```text
仅本地
    │
    ├─ 用户开启云端同步
    │       └─ 待同步
    │
待同步
    │
    ├─ 有网 + 登录 + 全局同步开启
    │       └─ 同步中
    │
同步中
    │
    ├─ 上传成功 → 已同步
    ├─ 网络失败 → 同步失败
    ├─ Token 失效 → 登录过期 / 本地模式
    └─ 云端版本冲突 → 冲突

冲突
    │
    ├─ 保留本地 → 覆盖云端 → 已同步
    ├─ 保留云端 → 覆盖本地 → 已同步
    └─ 稍后处理 → 保持冲突状态
```

---

## 8.2 全局同步设置

```text
同步设置
    │
    ├─ 全局同步开关 OFF → 暂停所有自动同步
    ├─ 全局同步开关 ON
    │       ├─ 仅 Wi-Fi ON → Wi-Fi 下同步
    │       └─ 仅 Wi-Fi OFF → 任意网络同步
    ├─ 冲突列表 → 逐条处理
    ├─ 失败列表 → 重试
    └─ 同步日志 → 查看记录
```

---

# 9. 附件状态流

```text
Markdown 渲染遇到图片/附件相对路径
    │
    ├─ App 内知识库 / 系统文件夹知识库
    │       └─ 按本地相对路径解析并展示
    │
    └─ 云端/Web 同步文档
            └─ 调用后端 resolve API 获取附件下载地址
```

上传附件：

```text
选择附件
    │
    ├─ 文件 ≤ 10MB → 上传并记录 relativePath
    └─ 文件 > 10MB → 提示暂不支持同步，保留本地引用
```

---

# 10. Todo 状态流

```text
Markdown Toolbar 点击日历
    │
    └─ 日历弹窗
            │
            ├─ 展示当天待办摘要
            ├─ 点击待办 → TodoActivity
            └─ 点击新增 → 新建 Todo
```

TodoActivity：

```text
Todo 列表
    │
    ├─ 新建 Todo
    ├─ 编辑 Todo
    ├─ 标记完成 / 取消完成
    ├─ 删除 Todo
    └─ 点击关联文档 → DocumentDetailActivity
```

AI 提取待办：

```text
AI 回复 / 文档总结
    │
    ├─ 提取待办草稿
    ├─ 用户确认 / 编辑
    └─ 保存到 Todo 列表
```

---

# 11. AI 状态机

```text
进入 AI Tab
    │
    ├─ 未登录
    │       └─ 显示登录引导
    │
    ├─ 已登录但未配置 Provider
    │       └─ 显示配置 AI Key 引导
    │
    └─ 已登录且已配置 Provider
            └─ 展示会话列表 / 当前会话
```

会话状态：

```text
无会话
    └─ 新建会话

有会话
    ├─ 切换会话
    ├─ 删除会话
    ├─ 发送消息
    │     ├─ Loading
    │     ├─ 成功 → 保存 user/assistant 消息到云端
    │     └─ 失败 → 显示错误，可重试
    └─ 复制 / 提取待办 / 生成 Markdown
```

---

# 12. Profile 状态流

```text
ProfileFragment
    │
    ├─ 未登录
    │       ├─ 显示本地模式说明
    │       └─ 登录入口
    │
    └─ 已登录
            ├─ 用户信息
            ├─ 修改昵称 / 头像
            ├─ AI Key 配置
            ├─ 同步设置
            ├─ 主题设置
            ├─ Markdown 渲染偏好
            ├─ 清理缓存
            ├─ 通知设置
            ├─ 关于
            ├─ 退出登录
            └─ 账号注销
```

退出登录：

```text
点击退出登录
    │
    ├─ 确认
    ├─ 清除 Token
    └─ 回到本地模式，不删除本地知识库
```

账号注销：

```text
点击账号注销
    │
    ├─ 强确认
    ├─ 请求后端注销
    ├─ 清除 Token
    └─ 保留本地知识库，云端数据按后端策略删除
```

---

# 13. 401 状态处理

```text
任何 API 返回 401
    │
    ├─ 清除本地 Token
    ├─ 展示 Snackbar：登录已过期
    ├─ AI / 云同步暂停
    └─ App 继续本地模式
```

不得强制用户离开当前页面。

---

# 14. 网络异常状态处理

```text
网络失败
    │
    ├─ Markdown 本地数据继续可用
    ├─ Todo 本地数据继续可用
    ├─ 同步任务进入失败队列
    ├─ AI 请求显示失败，可重试
    └─ Web 端显示网络错误
```

---

# 15. 关键验收状态

```text
未登录也能进入 MainActivity 并管理本地知识库
Token 过期后不影响本地阅读和编辑
同步失败不丢本地改动
冲突必须等待用户选择，不自动覆盖
系统文件夹删除必须强提示
AI 未配置 Key 时不能发送请求
附件超过 10MB 时必须提示并阻止云端上传
```
