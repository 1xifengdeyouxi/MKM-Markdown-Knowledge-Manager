# MKM 页面状态流转与导航规范

## 1. 文档目标

本文档定义 MKM Android 端的状态流转图和页面导航规范，补充 UI 设计文档中每个页面的状态机。

---

# 2. 全局用户状态机

```text
App 启动
    │
    ├─ 本地有 Token ──→ Token 验证
    │                      │
    │                ├─ 有效 ──→ MainActivity
    │                └─ 过期 ──→ 清除 Token → LoginActivity
    │
    └─ 无 Token ──→ LoginActivity
```

---

# 3. LoginActivity 状态机

```text
[初始状态]
    │
    ├─ 点击登录
    │      │
    │      ├─ 校验不通过 ──→ 输入框错误提示
    │      │
    │      └─ 校验通过 ──→ [加载中]
    │                          │
    │                    ├─ 成功 ──→ 保存 Token → MainActivity
    │                    └─ 失败 ──→ Snackbar 错误 → [初始状态]
    │
    └─ 点击注册（逻辑同上）
```

---

# 4. MarkdownFragment 状态机

```text
[进入页面]
    │
    ├─ 读取本地缓存 ──→ 立即展示缓存文档
    │
    └─ 请求后端
            │
      ├─ 成功 ──→ 更新缓存 → 刷新列表
      └─ 失败
              │
        ├─ 有缓存 ──→ Snackbar "同步失败，显示本地数据"
        └─ 无缓存 ──→ 空状态 + 重试按钮

[用户操作]
    │
    ├─ 点击文档 ──→ DocumentDetailActivity（预览模式）
    ├─ 点击 FAB ──→ DocumentDetailActivity（新建模式）
    ├─ 长按文档 ──→ 弹出菜单（编辑/删除/设置公开）
    └─ 下拉刷新 ──→ 重新请求后端
```

---

# 5. DocumentDetailActivity 状态机

## 5.1 新建模式

```text
[进入新建模式]
    │
    └─ 展示空标题 + 空内容编辑器
            │
      ─ 输入标题和内容
            │
      ─ 点击保存
            │
        ├─ 标题为空 ──→ 输入框错误提示
        └─ 校验通过 ──→ [保存中]
                            │
                      ├─ 成功 ──→ 跳转预览模式（当前文档）
                      └─ 失败 ──→ Snackbar 错误 → 保持编辑模式
```

## 5.2 预览模式

```text
[进入预览模式]
    │
    └─ 加载文档详情 → Markwon 渲染
            │
      ─ 点击编辑 ──→ 切换到编辑模式（当前内容填入输入框）
      ─ 点击 AI 总结 ──→ AI 总结弹窗（见 5.3）
      ─ 点击提取待办 ──→ 待办提取弹窗（见 5.4）
      ─ 点击更多菜单 ──→ 删除/设置公开
```

## 5.3 AI 总结弹窗状态机

```text
[触发 AI 总结]
    │
    └─ 弹出 BottomSheet
            │
      ─ [加载中动画]
            │
        ├─ 成功 ──→ 展示总结 Markdown
        │               │
        │         ├─ 复制 ──→ 复制到剪贴板 + Toast
        │         ├─ 保存为文档 ──→ POST /api/documents → 成功提示
        │         └─ 提取待办 ──→ 待办提取弹窗（5.4）
        │
        └─ 失败 ──→ 显示错误 + 重试按钮
```

## 5.4 待办提取弹窗状态机

```text
[触发提取待办]
    │
    └─ 弹出 BottomSheet
            │
      ─ 展示 AI 返回的待办草稿（带 CheckBox）
            │
        ─ 用户勾选
            │
        ─ 点击"保存选中"
            │
            └─ POST /api/todos/batch
                        │
                  ├─ 成功 ──→ Snackbar "已添加 N 条待办 [去查看]" → 关闭弹窗
                  └─ 失败 ──→ Snackbar 错误提示
```

---

# 6. TodoFragment 状态机

```text
[进入页面]
    │
    └─ 请求 GET /api/todos
            │
        ├─ 成功 ──→ 分 Tab 展示未完成/已完成
        └─ 失败 ──→ 空状态 + 重试

[用户操作]
    │
    ├─ 点击圆圈 ──→ PUT /api/todos/{id}/complete 或 uncomplete
    │                    │
    │               ├─ 成功 ──→ 动画 + 移至对应 Tab
    │               └─ 失败 ──→ Snackbar 错误
    │
    ├─ 点击 FAB ──→ 新建待办 BottomSheet
    │                    │
    │               ─ 填写内容 → 保存 → POST /api/todos
    │                    │
    │               ├─ 成功 ──→ 插入列表顶部
    │               └─ 失败 ──→ Snackbar 错误
    │
    └─ 滑动删除 ──→ DELETE /api/todos/{id}
                        │
                  ├─ 成功 ──→ 移除列表项
                  └─ 失败 ──→ 撤销滑动 + 错误提示
```

---

# 7. AiAssistantFragment 状态机

```text
[进入页面]
    │
    ├─ 检查 GET /api/ai/providers
    │       │
    │  ├─ 空 ──→ 展示空状态（引导配置）
    │  └─ 有配置 ──→ 展示聊天界面
    │
    └─ 进入聊天界面
                │
          ─ 用户输入并发送
                │
            ─ 输入框 disabled，发送按钮 disabled
                │
            ─ 消息列表加入 typing 动画
                │
            ─ POST /api/ai/chat
                │
          ├─ 成功 ──→ 移除 typing 动画，加入 AI 消息气泡
          │               │
          │         ─ 长按操作：复制/保存文档/提取待办
          │
          └─ 失败
                  │
            ├─ Provider 无效 ──→ "模型配置有误 [去检查]"
            ├─ 网络超时 ──→ "请求超时 [重试]"
            └─ 其他错误 ──→ Snackbar 提示
```

## 7.1 快捷按钮状态

| 按钮 | 触发行为 |
|---|---|
| 生成 Markdown | 弹出输入框，请求 POST /api/ai/markdown/generate |
| 提取待办 | 弹出输入框或用当前对话上下文，进入待办提取流程 |
| 代码解释 | 弹出粘贴代码输入框，走普通提问流程 |
| 技术笔记 | 弹出主题输入框，请求 POST /api/ai/markdown/generate |

## 7.2 生成 Markdown 后的操作流

```text
AI 返回 Markdown 内容
    │
    └─ 展示在消息气泡中（Markdown 渲染）
            │
        ─ 底部操作栏：[复制] [保存为文档] [继续编辑]
            │
    ─ 点击"保存为文档"
            │
        POST /api/documents { title: suggestedTitle, content }
            │
        ├─ 成功 ──→ Snackbar "已保存 [查看]" → 可跳转 DocumentDetailActivity
        └─ 失败 ──→ Snackbar 错误
```

---

# 8. AiProviderSettingsActivity 状态机

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
        ─ 打开添加表单 Activity 或弹窗
                │
        ─ 选择预设 ──→ 自动填充 baseUrl 和 modelName
                │
        ─ 填写 API Key
                │
        ─ 点击"测试连接"
                │
            POST /api/ai/providers/{id}/test
                │
          ├─ 成功 ──→ 显示"✅ 连接成功" + 允许保存
          └─ 失败 ──→ 显示"❌ 失败原因" + 仍可保存
                │
        ─ 点击保存 ──→ POST /api/ai/providers
                │
          ├─ 成功 ──→ 返回列表 + 刷新
          └─ 失败 ──→ Snackbar 错误
```

---

# 9. 全局 Token 过期处理

在 Retrofit 的 Response Interceptor 中统一拦截 401：

```text
收到 HTTP 401
    │
    └─ 清除本地 Token
            │
        ─ 弹出 Dialog "登录已过期，请重新登录"
                │
        ─ 用户点确认
                │
        ─ 跳转 LoginActivity + 清除返回栈
```

---

# 10. 网络状态处理

## 10.1 有 Room 缓存的页面

```text
文档列表

请求中 → 先展示 Room 缓存 → 请求完成后更新
```

## 10.2 无 Room 缓存的页面

```text
AI 聊天、待办、Provider 设置

无缓存 → 加载中 → 成功/失败
失败 → 空状态 + Snackbar + 重试
```

## 10.3 操作失败后的 Undo

待办滑动删除：

```text
滑动删除 → 移除 Item → Snackbar "已删除 [撤销]"
→ 用户点撤销 → 重新插入列表 + 取消删除请求
→ Snackbar 超时 → 确认执行删除请求
```

---

# 11. 当前结论

MKM Android V1 状态流转规范完成。

每个页面的状态机设计原则：

```text
先读缓存，后请求网络（有缓存的页面）
操作中 disabled 防重复提交
失败用 Snackbar 提示，不清空已有数据
Token 过期统一跳转登录页
AI 操作结果优先用 BottomSheet 展示，不覆盖主页面
```

关键 Android 组件对应关系：

```text
BottomSheet → AI 总结、待办提取、新建待办
Dialog → Token 过期、确认删除
Snackbar → 操作反馈、错误提示
LinearProgressIndicator → 页面加载
CircularProgressIndicator → 局部加载
SwipeRefreshLayout → 下拉刷新
RecyclerView ItemTouchHelper → 滑动删除待办
```
