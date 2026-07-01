# MKM 页面状态流转与导航规范

## 1. 文档目标

本文档定义 MKM Android 端的状态流转图和页面导航规范，补充 UI 设计文档中每个页面的状态机。

---

# 2. 全局启动状态机

```text
App 启动
    │
    ├─ 已完成仓库初始化（本地标记存在）
    │       │
    │       ├─ 无 Token（未登录）──→ MainActivity（本地模式）
    │       │
    │       └─ 有 Token ──→ Token 验证
    │                           │
    │                     ├─ 有效 ──→ MainActivity（已登录）
    │                     └─ 过期 ──→ 清除 Token → MainActivity（本地模式）
    │
    └─ 未完成仓库初始化
            └─ OnboardingActivity
```

核心变化：

```text
App 第一屏不再是 LoginActivity，而是判断是否完成仓库初始化。
登录不是强制的，本地模式可以完整使用 App。
Token 过期时不强制跳登录页，而是退为本地模式，允许继续使用。
```

---

# 3. OnboardingActivity 状态机

## 3.1 欢迎页

```text
[欢迎页]
    │
    ├─ 点击创建新仓库 ──→ 同步选择页
    └─ 点击使用现有仓库 ──→ 仓库来源选择页
```

## 3.2 同步选择页（创建新仓库路径）

```text
[同步选择页]
    │
    ├─ 点击开启云同步 ──→ LoginActivity（登录/注册）
    │                           │
    │                     ├─ 登录成功 ──→ 仓库信息设置页
    │                     └─ 点击"暂不同步" ──→ 返回同步选择页，选本地
    │
    └─ 点击暂不启用同步 ──→ 仓库信息设置页
```

## 3.3 仓库来源选择页（使用现有仓库路径）

```text
[来源选择页]
    │
    ├─ 点击打开本地仓库
    │       │
    │       └─ 调用 ACTION_OPEN_DOCUMENT_TREE
    │               │
    │         ├─ 用户选择目录
    │         │       ├─ 校验为 MKM 仓库 ──→ 导入仓库 → MainActivity
    │         │       └─ 不是 MKM 仓库 ──→ Snackbar 提示 → 返回选择
    │         └─ 用户取消 ──→ 返回来源选择页
    │
    └─ 点击从账号恢复 ──→ LoginActivity
                                │
                          ├─ 登录成功
                          │       └─ 请求云端仓库列表
                          │               ├─ 有仓库 ──→ 选择仓库 → 同步 → MainActivity
                          │               └─ 无仓库 ──→ 引导创建新仓库 → 仓库信息设置页
                          └─ 点击"暂不同步" ──→ 返回来源选择页
```

## 3.4 仓库信息设置页

```text
[仓库信息设置页]
    │
    └─ 输入仓库名称 + 选择存储位置
            │
        ─ 点击"创建仓库"
            │
        ─ 校验名称
            │
          ├─ 名称为空 ──→ 输入框错误提示
          ├─ 名称含非法字符 ──→ 输入框错误提示
          └─ 校验通过 ──→ [创建中]
                              │
                        ├─ 成功 ──→ 写入本地初始化标记 → MainActivity
                        ├─ 无权限 ──→ Snackbar "无法访问该目录，请重新选择"
                        └─ 其他失败 ──→ Snackbar 错误 + 重试
```

---

# 4. LoginActivity 状态机

LoginActivity 不再是 App 启动的第一屏，只在以下场景出现：

```text
Onboarding 选择开启云同步
Onboarding 从账号恢复仓库
ProfileFragment 点击"登录 / 注册"
```

```text
[初始状态]
    │
    ├─ 点击登录
    │      │
    │      ├─ 字段校验失败 ──→ 输入框错误提示
    │      └─ 校验通过 ──→ [加载中]
    │                          │
    │                    ├─ 成功 ──→ 保存 Token → 返回调用方（Onboarding 或 Profile）
    │                    └─ 失败 ──→ Snackbar 错误 → [初始状态]
    │
    ├─ 点击注册（逻辑同上）
    │
    └─ 点击"暂不同步，继续本地使用"
            └─ 退出登录页，返回 Onboarding 的上一步
```

---

# 5. MarkdownFragment 状态机

```text
[进入页面]
    │
    ├─ 读取本地仓库文档缓存 ──→ 立即展示
    │
    └─ 若已登录，请求后端同步
            │
      ├─ 成功 ──→ 更新缓存 → 刷新列表
      └─ 失败
              │
        ├─ 有缓存 ──→ Snackbar "同步失败，显示本地数据"
        └─ 无缓存 ──→ 空状态 + 重试按钮

[Tab 切换]
    │
    ├─ 最近 Tab ──→ 展示最近更新文档列表
    └─ 目录 Tab ──→ 展示仓库目录树

[用户操作]
    │
    ├─ 点击文档 ──→ DocumentDetailActivity（预览模式）
    ├─ 点击 FAB ──→ DocumentDetailActivity（新建模式）
    ├─ 长按文档 ──→ 弹出菜单（编辑/删除/重命名/设为公开/移动到目录）
    ├─ 点击搜索图标 ──→ Toolbar 进入搜索状态
    ├─ 点击列表设置 ──→ 底部弹出排序/筛选/视图切换 BottomSheet
    └─ 下拉刷新 ──→ 触发同步（已登录）或重新扫描仓库目录（本地模式）
```

---

# 6. DocumentDetailActivity 状态机

## 6.1 新建模式

```text
[进入新建模式]
    │
    └─ 展示空标题 + 空标签行 + 空内容编辑器 + Markdown 辅助工具栏
            │
      ─ 输入标题和内容
            │
      ─ 点击保存
            │
        ├─ 标题为空 ──→ 输入框错误提示
        └─ 校验通过 ──→ [保存中]
                            │
                      ├─ 成功 ──→ 切换到预览模式（当前文档）
                      └─ 失败 ──→ Snackbar 错误 → 保持编辑模式
```

## 6.2 预览模式

```text
[进入预览模式]
    │
    └─ 加载文档详情 → Markwon 渲染
            │
      ─ 点击编辑 ──→ 切换到编辑模式（当前内容填入输入框）
      ─ 点击 AI 总结 ──→ AI 总结 BottomSheet（见 6.4）
      ─ 点击提取待办 ──→ 待办提取 BottomSheet（见 6.5）
      ─ 点击更多菜单 [⋮]
              │
          ├─ 编辑标签 ──→ 标签输入弹窗
          ├─ 设为公开/私有 ──→ 调用 API → 更新文档状态
          ├─ 分享 ──→ 系统分享面板
          ├─ 导出 Markdown ──→ 系统文件保存
          ├─ 查看属性 ──→ 弹出属性 BottomSheet
          └─ 删除文档 ──→ 确认 Dialog → 删除 → 返回列表
```

## 6.3 编辑模式

```text
[进入编辑模式]
    │
    └─ 加载当前文档内容到输入框 + 标签行 + Markdown 辅助工具栏
            │
      ─ 点击保存
            │
        ├─ 标题为空 ──→ 输入框错误提示
        └─ 校验通过 ──→ [保存中]
                            │
                      ├─ 成功 ──→ Snackbar "已保存" → 切换到预览模式
                      └─ 失败 ──→ Snackbar 错误 → 保持编辑模式

[退出编辑模式（返回键）]
    │
    ├─ 内容无修改 ──→ 直接退出
    └─ 内容有修改 ──→ Dialog "是否保存更改？"
                            │
                      ├─ 保存 ──→ 执行保存 → 退出
                      ├─ 放弃 ──→ 丢弃修改 → 退出
                      └─ 取消 ──→ 继续编辑
```

## 6.4 AI 总结 BottomSheet 状态机

```text
[触发 AI 总结]
    │
    └─ 弹出 BottomSheet
            │
      ─ [加载中动画]
            │
        ├─ 成功 ──→ Markdown 渲染总结内容
        │               │
        │         ├─ 复制 ──→ 复制到剪贴板 + Toast
        │         ├─ 保存为文档 ──→ POST /api/documents → Snackbar 成功提示
        │         └─ 提取待办 ──→ 关闭当前 BottomSheet → 待办提取 BottomSheet（6.5）
        │
        └─ 失败 ──→ 显示错误信息 + [重试]
```

## 6.5 待办提取 BottomSheet 状态机

```text
[触发提取待办]
    │
    └─ 弹出 BottomSheet
            │
      ─ [AI 加载中]
            │
        ├─ 成功 ──→ 展示 AI 返回的待办草稿（带 CheckBox）
        │               │
        │         ─ 用户勾选/取消勾选
        │               │
        │         ─ 点击"保存选中"
        │               │
        │               └─ POST /api/todos/batch
        │                           │
        │                     ├─ 成功 ──→ Snackbar "已添加 N 条待办 [去查看]" → 关闭
        │                     └─ 失败 ──→ Snackbar 错误
        │
        └─ 失败 ──→ 显示错误信息 + [重试]
```

---

# 7. TodoFragment 状态机

```text
[进入页面]
    │
    └─ 请求 GET /api/todos（已登录）或读取本地待办（本地模式）
            │
        ├─ 成功 ──→ 分 Tab 展示未完成/已完成
        └─ 失败 ──→ 空状态 + 重试

[Tab 切换]
    │
    ├─ 未完成 Tab ──→ 展示未完成待办列表
    └─ 已完成 Tab ──→ 展示已完成待办列表

[用户操作]
    │
    ├─ 点击圆圈 ──→ PUT /api/todos/{id}/complete 或 uncomplete
    │                    │
    │               ├─ 成功 ──→ 划线动画 + 移至对应 Tab
    │               └─ 失败 ──→ Snackbar 错误 + 恢复状态
    │
    ├─ 点击待办行 ──→ 展开详情 BottomSheet
    │                    │
    │               ─ 点击来源文档 ──→ 跳转 DocumentDetailActivity
    │               ─ 点击编辑 ──→ 进入编辑状态
    │               ─ 点击删除 ──→ 确认 Dialog → DELETE /api/todos/{id}
    │
    ├─ 点击 FAB ──→ 新建待办 BottomSheet
    │                    │
    │               ─ 输入内容 + 优先级 + 截止时间（可选）+ 备注（可选）
    │               ─ 点击保存 ──→ POST /api/todos
    │                    │
    │               ├─ 成功 ──→ 插入未完成列表顶部 + 关闭弹窗
    │               └─ 失败 ──→ Snackbar 错误
    │
    └─ 滑动删除
            │
        ─ 乐观 UI：立即移除列表项 + Snackbar "已删除 [撤销]"（5秒）
            │
        ├─ 用户点撤销（5秒内）──→ 取消删除请求 + 重新插入列表
        └─ Snackbar 超时 ──→ 执行 DELETE /api/todos/{id}
                                    │
                              └─ 失败 ──→ Snackbar 错误 + 重新插入列表
```

---

# 8. AiAssistantFragment 状态机

```text
[进入页面]
    │
    ├─ 检查 Provider 配置
    │       │
    │  ├─ 无配置 ──→ 展示空状态（引导配置 API Key）
    │  └─ 有配置 ──→ 检查当前对话历史
    │                   │
    │              ├─ 有历史 ──→ 展示历史消息列表
    │              └─ 无历史 ──→ 展示快捷操作卡片
    │
    └─ 对话中
            │
        ─ 用户输入并发送
            │
        ─ 输入框 disabled + 发送按钮 disabled
            │
        ─ 消息列表加入 typing 动画气泡
            │
        ─ POST /api/ai/chat
            │
        ├─ 成功 ──→ 移除 typing 动画 + 渲染 AI 消息气泡
        │               │
        │         ─ 快捷操作：[复制] [保存文档] [提取待办]
        │
        └─ 失败
                │
          ├─ Provider 无效 ──→ 消息气泡内显示错误 + "模型配置有误 [去检查]"
          ├─ 网络超时 ──→ 消息气泡内显示 "请求超时 [重试]"
          └─ 其他错误 ──→ Snackbar 提示

[新建对话]
    │
    └─ 点击 Toolbar [+] ──→ 保存当前对话到历史 → 清空消息列表 → 展示快捷操作

[切换历史对话]
    │
    └─ 点击更多菜单 ──→ 历史对话列表 BottomSheet
            │
        ─ 点击某条历史 ──→ 加载历史消息 → 展示历史对话
```

## 8.1 快捷操作弹窗状态机

```text
[触发快捷操作]
    │
    ├─ 生成 Markdown
    │       └─ 弹出主题输入框 Dialog
    │               ─ 用户输入 → 确认
    │               └─ 发送 POST /api/ai/markdown/generate → 同普通发送流程
    │
    ├─ 提取待办
    │       └─ 弹出文本输入框 Dialog（粘贴文本或输入主题）
    │               ─ 用户输入 → 确认
    │               └─ AI 返回待办草稿 → 进入待办提取 BottomSheet
    │
    ├─ 代码解释
    │       └─ 弹出多行代码输入框 Dialog
    │               ─ 用户粘贴代码 → 确认
    │               └─ 发送 POST /api/ai/chat → 同普通发送流程
    │
    └─ 技术笔记
            └─ 弹出主题输入框 Dialog
                    ─ 用户输入 → 确认
                    └─ 发送 POST /api/ai/markdown/generate → 同普通发送流程
```

---

# 9. KnowledgeFragment 状态机

```text
[进入页面]
    │
    └─ 请求 GET /api/documents/public
            │
        ├─ 成功 ──→ 展示公开文档列表 + 标签筛选栏
        └─ 失败 ──→ 空状态 + Snackbar + 重试

[用户操作]
    │
    ├─ 点击搜索 ──→ Toolbar 搜索模式
    │               ─ 输入关键词 ──→ GET /api/documents/public?q=xxx
    │
    ├─ 点击标签筛选 ──→ GET /api/documents/public?tag=kotlin
    │
    └─ 点击文档卡片 ──→ 只读文档详情页
                            │
                      ─ 点击 AI 总结 ──→ AI 总结 BottomSheet
                      ─ 点击点赞
                      │       │
                      │  ├─ 未登录 ──→ Snackbar "登录后才能点赞"
                      │  └─ 已登录 ──→ POST /api/documents/{id}/like → 数字更新
                      │
                      ─ 发表评论
                              │
                        ├─ 未登录 ──→ 显示"登录后才能评论 [去登录]"
                        └─ 已登录 ──→ POST /api/documents/{id}/comments
                                            │
                                      ├─ 成功 ──→ 插入评论列表顶部
                                      └─ 失败 ──→ Snackbar 错误
```

---

# 10. ProfileFragment 状态机

```text
[进入页面]
    │
    ├─ 检查登录状态
    │       │
    │  ├─ 未登录 ──→ 账号区显示登录入口，仓库/AI/外观功能正常展示
    │  └─ 已登录 ──→ 账号区展示头像 + 用户名 + 邮箱

[用户操作]
    │
    ├─ 点击登录入口（未登录状态）──→ LoginActivity
    │                                   │
    │                             └─ 登录成功 ──→ 返回 ProfileFragment 刷新账号区
    │
    ├─ 点击当前仓库 ──→ 仓库详情页
    │
    ├─ 点击同步状态
    │       │
    │  ├─ 未登录 ──→ 显示"当前本地模式" + [开启云同步]
    │  └─ 已登录 ──→ 展示同步详情 + [立即同步] + [关闭云同步]
    │
    ├─ 点击 AI 模型配置 ──→ AiProviderSettingsActivity
    │
    ├─ 点击主题 ──→ 主题选择 BottomSheet → 立即生效
    │
    ├─ 点击语言 ──→ 语言选择 BottomSheet → 重启 App 生效（或即时生效）
    │
    ├─ 点击导出仓库 ──→ 导出确认 Dialog
    │                       │
    │                 └─ 确认 ──→ 打包 ZIP → 系统分享/文件保存
    │
    └─ 点击退出登录
            │
        ─ 确认 Dialog
            │
        ─ 清除 Token
            │
        ─ ProfileFragment 账号区切换为"未登录"状态
            （不清除本地仓库数据，不强制跳登录页）
```

---

# 11. AiProviderSettingsActivity 状态机

```text
[进入页面]
    │
    └─ GET /api/ai/providers
            │
        ├─ 空 ──→ 空状态 + 引导添加
        └─ 有 ──→ 展示配置列表

[添加 Provider]
    │
    └─ 点击"添加模型"
            │
        ─ 展示添加表单
                │
        ─ 点击预设 ──→ 自动填充 baseUrl 和 modelName
                │
        ─ 填写 API Key
                │
        ─ 点击"测试连接"
                │
            POST /api/ai/providers/test
                │
          ├─ 成功 ──→ 显示"✅ 连接成功（耗时 1200ms）"
          └─ 失败 ──→ 显示"❌ 失败：Invalid API key"
                │
        ─ 点击保存 ──→ POST /api/ai/providers
                │
          ├─ 成功 ──→ 返回列表 + 刷新
          └─ 失败 ──→ Snackbar 错误

[删除 Provider]
    │
    └─ 点击删除 ──→ 确认 Dialog
                        │
                  └─ 确认 ──→ DELETE /api/ai/providers/{id}
                                    │
                              ├─ 成功 ──→ 从列表移除
                              └─ 失败 ──→ Snackbar 错误
```

---

# 12. 全局 Token 过期处理

Token 过期策略调整：

```text
旧策略：过期 → 强制跳 LoginActivity
新策略：过期 → 清除 Token → 退为本地模式 → 弹 Snackbar "登录已过期" [重新登录]
```

在 Retrofit 的 Response Interceptor 中统一拦截 401：

```text
收到 HTTP 401
    │
    └─ 清除本地 Token
            │
        ─ Snackbar 提示 "登录已过期 [重新登录]"（全局）
                │
        ─ 用户点"重新登录" ──→ 跳转 LoginActivity
        ─ 用户不点 ──→ App 继续运行（降级为本地模式）
```

只有在用户主动点击"重新登录"时才跳转登录页。

---

# 13. 网络状态处理

## 13.1 有本地缓存的页面

```text
文档列表（MarkdownFragment）

进入页面 → 立即展示本地缓存 → 若已登录，后台请求同步 → 成功后静默刷新列表
```

## 13.2 无缓存的页面

```text
AI 聊天历史、待办（纯云端）、Provider 设置

加载中 ──→ 成功/失败
失败 ──→ 空状态 + Snackbar "加载失败 [重试]"
```

## 13.3 操作失败后的 Undo

待办滑动删除：

```text
滑动删除
    │
    └─ 乐观 UI：立即移除列表项 + Snackbar "已删除 [撤销]"（5秒）
            │
        ├─ 用户 5 秒内点撤销 ──→ 取消请求 + 重新插入列表
        └─ 5 秒超时 ──→ 执行 DELETE 请求
                                │
                          └─ 失败 ──→ Snackbar 错误 + 重新插入
```

---

# 14. 全局导航关系图

```text
OnboardingActivity
    └──→ LoginActivity（开启同步 / 从账号恢复）
    └──→ MainActivity

LoginActivity
    └──→ 返回调用方（Onboarding / Profile）

MainActivity
    ├── MarkdownFragment（默认）
    │       └──→ DocumentDetailActivity（预览/编辑/新建）
    │
    ├── TodoFragment
    │
    ├── AiAssistantFragment
    │       └──→ AiProviderSettingsActivity（未配置时）
    │       └──→ DocumentDetailActivity（保存生成文档）
    │
    ├── KnowledgeFragment
    │       └──→ 只读 DocumentDetailActivity
    │
    └── ProfileFragment
            └──→ LoginActivity（未登录时）
            └──→ AiProviderSettingsActivity
```

---

# 15. 当前结论

MKM Android V1 状态流转规范（更新版）。

核心设计原则：

```text
仓库初始化是第一步，登录是可选的
Token 过期不强制跳登录，降级为本地模式
本地缓存优先，后台静默同步
乐观 UI + Undo：删除操作先移除，支持 5 秒内撤销
操作中 disabled 防重复提交
失败用 Snackbar 提示，不清空已有数据
AI 操作结果优先用 BottomSheet 展示
```

关键 Android 组件对应关系：

```text
BottomSheet → AI 总结、待办提取、新建待办、列表设置、模型切换
Dialog → 确认删除、未保存提示、导出确认
Snackbar → 操作反馈、错误提示、撤销入口
LinearProgressIndicator → 页面初始加载
CircularProgressIndicator → 局部操作加载
SwipeRefreshLayout → 下拉同步刷新
RecyclerView ItemTouchHelper → 滑动删除待办
ACTION_OPEN_DOCUMENT_TREE → 仓库目录选择
```
