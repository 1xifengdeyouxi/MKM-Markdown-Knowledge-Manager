# MKM AI Provider 配置设计

## 1. 文档目标

本文档定义 MKM 中用户配置 AI 模型 Provider 的方案，包括：

- 产品交互
- 配置字段
- 支持的 Provider 类型
- DeepSeek / MiniMax 等国内模型配置示例
- API Key 安全存储
- 后端接口设计
- Android / Web 页面建议

MKM 采用：

```text
预设 Provider + 自定义 Base URL
```

也就是：

- 普通用户可以从预设模型中选择
- 程序员用户可以自定义 Base URL 和模型名
- 后端统一按 OpenAI-compatible 优先适配

---

## 2. 设计原则

## 2.1 BYOK：用户自带 API Key

MKM 不默认提供模型额度，用户需要配置自己的 API Key。

优势：

- 降低项目方成本
- 适合学习项目和个人工具
- 用户可以选择自己喜欢的模型
- 方便支持国内模型

---

## 2.2 预设优先，自定义补充

用户进入 AI 设置页时，先看到常见 Provider 预设：

- DeepSeek
- MiniMax
- 通义千问
- 智谱 GLM
- OpenAI
- Anthropic / Claude（后续）
- Ollama（后续）
- 自定义 OpenAI-compatible

选择预设后，系统自动填充：

```text
providerType
baseUrl 示例
推荐 modelName
```

用户只需要填写：

```text
API Key
```

高级用户可以手动修改：

```text
Base URL
Model Name
```

---

## 2.3 OpenAI-compatible 优先

V1 后端优先实现一种通用 Provider：

```text
openai-compatible
```

只要模型供应商支持类似 OpenAI Chat Completions 的接口，就可以接入。

统一配置模型：

```text
baseUrl + apiKey + modelName
```

---

## 3. 用户配置流程

## 3.1 Android 流程

```text
我的
→ AI 设置
→ 添加模型
→ 选择 Provider 预设
→ 填写 API Key
→ 可选修改 Base URL / 模型名
→ 点击测试连接
→ 测试成功后保存
→ 设置为默认模型
```

## 3.2 Web 流程

```text
Web 登录
→ 我的 / 设置
→ AI Provider 管理
→ 添加 / 编辑 / 删除 Provider
→ 测试连接
→ 设置默认 Provider
```

## 3.3 未配置 Provider 时

AI 页面显示空状态：

```text
你还没有配置 AI 模型
请先添加 DeepSeek、MiniMax 或其他 OpenAI-compatible 模型 API Key。

按钮：去配置
```

---

## 4. Provider 预设

## 4.1 DeepSeek

| 字段 | 值 |
|---|---|
| providerName | DeepSeek |
| providerType | openai-compatible |
| baseUrl | https://api.deepseek.com |
| modelName | deepseek-chat |
| apiKey | 用户填写 |

可选模型：

```text
deepseek-chat
deepseek-reasoner
```

---

## 4.2 MiniMax

| 字段 | 值 |
|---|---|
| providerName | MiniMax |
| providerType | openai-compatible |
| baseUrl | 由用户按 MiniMax 控制台配置 |
| modelName | abab6.5s-chat / 具体以控制台为准 |
| apiKey | 用户填写 |

说明：MiniMax 不同版本 API 路径和模型名可能变化，因此 V1 允许用户自定义 Base URL 和 modelName。

---

## 4.3 通义千问

| 字段 | 值 |
|---|---|
| providerName | 通义千问 |
| providerType | openai-compatible |
| baseUrl | https://dashscope.aliyuncs.com/compatible-mode/v1 |
| modelName | qwen-plus |
| apiKey | 用户填写 |

可选模型：

```text
qwen-plus
qwen-turbo
qwen-max
```

---

## 4.4 智谱 GLM

| 字段 | 值 |
|---|---|
| providerName | 智谱 GLM |
| providerType | openai-compatible |
| baseUrl | https://open.bigmodel.cn/api/paas/v4 |
| modelName | glm-4-flash |
| apiKey | 用户填写 |

---

## 4.5 OpenAI

| 字段 | 值 |
|---|---|
| providerName | OpenAI |
| providerType | openai-compatible |
| baseUrl | https://api.openai.com/v1 |
| modelName | gpt-4o-mini |
| apiKey | 用户填写 |

---

## 4.6 自定义 OpenAI-compatible

| 字段 | 值 |
|---|---|
| providerName | 用户自定义 |
| providerType | openai-compatible |
| baseUrl | 用户填写 |
| modelName | 用户填写 |
| apiKey | 用户填写 |

适用场景：

- 公司内部模型代理
- One API / LiteLLM 网关
- OpenRouter
- 自建模型服务
- 其他兼容 OpenAI 的国内外模型

---

## 5. 配置字段定义

### ai_provider_configs

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | Long | 是 | 主键 |
| ownerId | Long | 是 | 所属用户 |
| providerName | String | 是 | 用户看到的名称 |
| providerType | String | 是 | openai-compatible / anthropic / ollama |
| baseUrl | String | 是 | API 基础地址 |
| encryptedApiKey | Text | 是 | 加密后的 API Key |
| maskedApiKey | String | 否 | 返回给前端的脱敏 Key |
| modelName | String | 是 | 模型名称 |
| enabled | Boolean | 是 | 是否启用 |
| defaultProvider | Boolean | 是 | 是否默认 |
| createdAt | DateTime | 是 | 创建时间 |
| updatedAt | DateTime | 是 | 更新时间 |

---

## 6. API Key 安全设计

## 6.1 基本原则

API Key 必须按敏感信息处理。

要求：

- 不在前端长期保存完整 API Key
- 不在日志中打印 API Key
- 后端数据库不保存明文 API Key
- 返回给前端时只返回脱敏结果
- 用户可以删除配置

---

## 6.2 加密存储方案

V1 推荐使用服务端对称加密：

```text
用户提交 API Key
→ 后端使用 APP_SECRET 加密
→ 保存 encryptedApiKey
→ 调用模型前解密
```

环境变量：

```text
AI_KEY_ENCRYPTION_SECRET=至少32位随机字符串
```

### 注意

- 加密密钥不要写死在代码里
- 本地开发可以在 application.yml 中提供默认值
- 生产环境必须使用环境变量

---

## 6.3 脱敏展示

前端展示：

```text
sk-abc123456789 → sk-abc****789
```

如果 Key 太短：

```text
****
```

---

## 7. Provider 配置 API

## 7.1 获取配置列表

```http
GET /api/ai/providers
```

响应：

```json
[
  {
    "id": 1,
    "providerName": "DeepSeek",
    "providerType": "openai-compatible",
    "baseUrl": "https://api.deepseek.com",
    "modelName": "deepseek-chat",
    "maskedApiKey": "sk-abc****789",
    "enabled": true,
    "defaultProvider": true
  }
]
```

---

## 7.2 新增 Provider

```http
POST /api/ai/providers
```

请求：

```json
{
  "providerName": "DeepSeek",
  "providerType": "openai-compatible",
  "baseUrl": "https://api.deepseek.com",
  "modelName": "deepseek-chat",
  "apiKey": "sk-xxxx",
  "defaultProvider": true
}
```

---

## 7.3 更新 Provider

```http
PUT /api/ai/providers/{id}
```

请求：

```json
{
  "providerName": "DeepSeek",
  "baseUrl": "https://api.deepseek.com",
  "modelName": "deepseek-reasoner",
  "apiKey": "sk-new-key-or-null",
  "enabled": true
}
```

说明：

```text
如果 apiKey 为空，则不更新原有 Key。
如果 apiKey 有值，则重新加密保存。
```

---

## 7.4 删除 Provider

```http
DELETE /api/ai/providers/{id}
```

删除规则：

- 只能删除自己的 Provider
- 如果删除默认 Provider，需要自动选择另一个可用 Provider 或提示用户重新设置

---

## 7.5 设置默认 Provider

```http
PUT /api/ai/providers/{id}/default
```

处理逻辑：

```text
将当前用户所有 Provider 的 defaultProvider 设为 false
将指定 Provider 设为 true
```

---

## 7.6 测试 Provider

```http
POST /api/ai/providers/{id}/test
```

后端发送一个轻量测试 Prompt：

```text
请回复 OK
```

响应：

```json
{
  "success": true,
  "message": "连接成功",
  "latencyMs": 1200
}
```

失败响应：

```json
{
  "success": false,
  "message": "API Key 无效或模型不可用"
}
```

---

## 8. 后端适配设计

## 8.1 Provider 类型枚举

V1：

```kotlin
enum class AiProviderType {
    OPENAI_COMPATIBLE
}
```

后续扩展：

```kotlin
enum class AiProviderType {
    OPENAI_COMPATIBLE,
    ANTHROPIC,
    OLLAMA
}
```

---

## 8.2 Provider Adapter 接口

```kotlin
interface AiProviderAdapter {
    fun supports(type: AiProviderType): Boolean
    fun chat(config: AiProviderConfig, messages: List<AiMessageInput>): AiProviderResponse
}
```

统一请求结构：

```kotlin
data class AiMessageInput(
    val role: String,
    val content: String
)
```

统一响应结构：

```kotlin
data class AiProviderResponse(
    val content: String,
    val modelName: String,
    val latencyMs: Long
)
```

---

## 8.3 OpenAI-compatible 请求格式

请求：

```json
{
  "model": "deepseek-chat",
  "messages": [
    { "role": "system", "content": "你是 MKM 的 AI 助手" },
    { "role": "user", "content": "帮我总结这段 Markdown" }
  ],
  "temperature": 0.7
}
```

路径：

```text
{baseUrl}/chat/completions
```

注意：

有些 Provider 的 baseUrl 已经包含 `/v1`，有些没有，因此 V1 配置时建议让用户填写完整兼容根路径。

示例：

```text
https://api.openai.com/v1
https://dashscope.aliyuncs.com/compatible-mode/v1
```

---

## 9. Android 页面设计

## 9.1 AI 设置入口

```text
我的
→ AI 设置
```

页面结构：

```text
AI 设置页
├── 当前默认模型
├── Provider 列表
│   ├── DeepSeek
│   ├── MiniMax
│   └── 自定义模型
├── 添加模型按钮
└── 测试连接按钮
```

---

## 9.2 添加 Provider 页面

字段：

```text
Provider 预设下拉选择
Provider 名称
Provider 类型
Base URL
Model Name
API Key
是否设为默认
测试连接
保存
```

---

## 9.3 空状态

如果未配置模型：

```text
暂无 AI 模型配置
你可以添加 DeepSeek、MiniMax 或其他 OpenAI-compatible 模型。

[添加模型]
```

---

## 10. Web 页面设计

Web 端更适合配置较长字段，因此 V1 推荐优先在 Web 端做完整 Provider 管理，Android 做基础配置或跳转提示。

Web 页面：

```text
AI Provider 管理
├── Provider 表格
├── 添加 Provider 弹窗
├── 编辑 Provider
├── 测试连接
└── 设置默认
```

---

## 11. 配置校验规则

| 字段 | 校验 |
|---|---|
| providerName | 必填，1-50 字符 |
| providerType | 必填，V1 只能是 openai-compatible |
| baseUrl | 必填，必须是 http/https URL |
| modelName | 必填 |
| apiKey | 新增时必填，更新时可为空 |
| defaultProvider | Boolean |

---

## 12. 推荐默认预设列表

V1 可以内置以下预设：

```json
[
  {
    "name": "DeepSeek",
    "providerType": "openai-compatible",
    "baseUrl": "https://api.deepseek.com",
    "models": ["deepseek-chat", "deepseek-reasoner"]
  },
  {
    "name": "通义千问",
    "providerType": "openai-compatible",
    "baseUrl": "https://dashscope.aliyuncs.com/compatible-mode/v1",
    "models": ["qwen-plus", "qwen-turbo", "qwen-max"]
  },
  {
    "name": "智谱 GLM",
    "providerType": "openai-compatible",
    "baseUrl": "https://open.bigmodel.cn/api/paas/v4",
    "models": ["glm-4-flash", "glm-4-plus"]
  },
  {
    "name": "OpenAI",
    "providerType": "openai-compatible",
    "baseUrl": "https://api.openai.com/v1",
    "models": ["gpt-4o-mini", "gpt-4o"]
  },
  {
    "name": "自定义",
    "providerType": "openai-compatible",
    "baseUrl": "",
    "models": []
  }
]
```

MiniMax 建议作为“自定义或半预设”处理，因为具体接口路径和模型名可能随平台版本变化。

---

## 13. 当前结论

MKM 的 AI Provider 配置采用：

```text
预设 Provider + 自定义 Base URL + 用户自带 API Key
```

V1 优先实现：

```text
OpenAI-compatible Provider
```

优先支持：

- DeepSeek
- 通义千问
- 智谱 GLM
- OpenAI
- 自定义 OpenAI-compatible
- MiniMax 作为可自定义配置支持

安全策略：

```text
API Key 后端加密保存，前端只显示脱敏结果，日志中禁止输出 Key。
```

这套方案既能降低普通用户配置难度，也能满足程序员用户接入 DeepSeek、MiniMax、公司内部模型或其他兼容 OpenAI API 的模型服务。
