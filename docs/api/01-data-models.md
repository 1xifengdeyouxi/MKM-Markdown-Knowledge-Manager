# MKM 数据模型设计

## 1. 文档目标

本文档定义 MKM V1 的完整数据库表结构。

设计原则：

- 以 PostgreSQL 为目标数据库
- JPA 实体映射
- V1 建表，V2/V3 只扩展不重建
- 字段用下划线命名（数据库），JPA 实体用驼峰

---

## 2. 总览

V1 包含以下核心表：

| 表名 | 说明 |
|---|---|
| users | 用户账号 |
| documents | Markdown 文档 |
| todos | 待办任务 |
| ai_provider_configs | 用户 AI 模型配置 |
| ai_conversations | AI 对话会话（可选，V1 建表备用） |
| ai_messages | AI 对话消息（可选，V1 建表备用） |
| ai_call_logs | AI 调用日志（可选） |

V2/V3 扩展表（本文档不实现，仅预留设计方向）：

| 表名 | 阶段 | 说明 |
|---|---|---|
| knowledge_bases | V3 | 公开知识库 |
| knowledge_base_documents | V3 | 知识库文档关联 |
| comments | V3 | 评论 |
| favorites | V3 | 收藏 |
| document_tags | V2 | 文档标签（当前 tags 字段升级） |
| tags | V2 | 标签表 |
| vector_chunks | V3 | 文档向量切片 |

---

# 3. users 表

```sql
CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL UNIQUE,
    password     VARCHAR(256) NOT NULL,
    email        VARCHAR(128) UNIQUE,
    avatar_url   VARCHAR(512),
    bio          TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键，自增 |
| username | VARCHAR(64) | 唯一用户名，不可重复 |
| password | VARCHAR(256) | BCrypt 加密后密码 |
| email | VARCHAR(128) | 可选，唯一 |
| avatar_url | VARCHAR(512) | 头像 URL，V2 使用 |
| bio | TEXT | 个人简介，V2 使用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### JPA 实体

```kotlin
@Entity @Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true, length = 64)
    val username: String,
    @Column(nullable = false, length = 256)
    var password: String,
    @Column(unique = true, length = 128)
    var email: String? = null,
    @Column(length = 512)
    var avatarUrl: String? = null,
    @Column(columnDefinition = "TEXT")
    var bio: String? = null,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
```

---

# 4. documents 表

```sql
CREATE TABLE documents (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(256) NOT NULL,
    content      TEXT NOT NULL DEFAULT '',
    summary      TEXT,
    is_public    BOOLEAN NOT NULL DEFAULT FALSE,
    tags         VARCHAR(512),
    owner_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_owner_id ON documents(owner_id);
CREATE INDEX idx_documents_is_public ON documents(is_public);
CREATE INDEX idx_documents_updated_at ON documents(updated_at DESC);
```

### 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| title | VARCHAR(256) | 文档标题，必填 |
| content | TEXT | Markdown 内容 |
| summary | TEXT | AI 生成摘要，可选 |
| is_public | BOOLEAN | 是否公开，默认 false |
| tags | VARCHAR(512) | 简单标签，逗号分隔，V2 可升级 |
| owner_id | BIGINT | 所属用户 FK |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### JPA 实体

```kotlin
@Entity @Table(name = "documents")
class Document(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 256)
    var title: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = "",
    @Column(columnDefinition = "TEXT")
    var summary: String? = null,
    @Column(nullable = false)
    var isPublic: Boolean = false,
    @Column(length = 512)
    var tags: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate fun onUpdate() { updatedAt = LocalDateTime.now() }
}
```

---

# 5. todos 表

```sql
CREATE TABLE todos (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(256) NOT NULL,
    description         TEXT,
    completed           BOOLEAN NOT NULL DEFAULT FALSE,
    priority            VARCHAR(16) DEFAULT 'medium',
    due_date            TIMESTAMP,
    source_type         VARCHAR(32) DEFAULT 'manual',
    source_document_id  BIGINT REFERENCES documents(id) ON DELETE SET NULL,
    owner_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_todos_owner_id ON todos(owner_id);
CREATE INDEX idx_todos_completed ON todos(completed);
```

### 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| title | VARCHAR(256) | 待办标题，必填 |
| description | TEXT | 描述，可选 |
| completed | BOOLEAN | 是否完成 |
| priority | VARCHAR(16) | low / medium / high |
| due_date | TIMESTAMP | 截止时间，可选 |
| source_type | VARCHAR(32) | manual / document / ai |
| source_document_id | BIGINT | 来源文档 FK，可选 |
| owner_id | BIGINT | 所属用户 FK |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### JPA 实体

```kotlin
@Entity @Table(name = "todos")
class Todo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 256)
    var title: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(nullable = false)
    var completed: Boolean = false,
    @Column(length = 16)
    var priority: String = "medium",
    var dueDate: LocalDateTime? = null,
    @Column(length = 32)
    var sourceType: String = "manual",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id")
    var sourceDocument: Document? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate fun onUpdate() { updatedAt = LocalDateTime.now() }
}
```

---

# 6. ai_provider_configs 表

```sql
CREATE TABLE ai_provider_configs (
    id                BIGSERIAL PRIMARY KEY,
    owner_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_name     VARCHAR(64) NOT NULL,
    provider_type     VARCHAR(32) NOT NULL DEFAULT 'openai-compatible',
    base_url          VARCHAR(512) NOT NULL,
    encrypted_api_key TEXT NOT NULL,
    model_name        VARCHAR(128) NOT NULL,
    enabled           BOOLEAN NOT NULL DEFAULT TRUE,
    default_provider  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_providers_owner_id ON ai_provider_configs(owner_id);
```

### 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| owner_id | BIGINT | 所属用户 FK |
| provider_name | VARCHAR(64) | 用户显示名，如 DeepSeek |
| provider_type | VARCHAR(32) | openai-compatible / anthropic |
| base_url | VARCHAR(512) | API 基础路径 |
| encrypted_api_key | TEXT | 加密后的 API Key |
| model_name | VARCHAR(128) | 模型名，如 deepseek-chat |
| enabled | BOOLEAN | 是否启用 |
| default_provider | BOOLEAN | 是否为默认模型 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### JPA 实体

```kotlin
@Entity @Table(name = "ai_provider_configs")
class AiProviderConfig(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,
    @Column(nullable = false, length = 64)
    var providerName: String,
    @Column(nullable = false, length = 32)
    var providerType: String = "openai-compatible",
    @Column(nullable = false, length = 512)
    var baseUrl: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    var encryptedApiKey: String,
    @Column(nullable = false, length = 128)
    var modelName: String,
    @Column(nullable = false)
    var enabled: Boolean = true,
    @Column(nullable = false)
    var defaultProvider: Boolean = false,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate fun onUpdate() { updatedAt = LocalDateTime.now() }
}
```

---

# 7. ai_conversations 表

V1 可选，建议预建表方便后续扩展。

```sql
CREATE TABLE ai_conversations (
    id                    BIGSERIAL PRIMARY KEY,
    owner_id              BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title                 VARCHAR(256) NOT NULL DEFAULT '新对话',
    source_type           VARCHAR(32) DEFAULT 'chat',
    source_document_id    BIGINT REFERENCES documents(id) ON DELETE SET NULL,
    provider_config_id    BIGINT REFERENCES ai_provider_configs(id) ON DELETE SET NULL,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_conversations_owner_id ON ai_conversations(owner_id);
```

### source_type 枚举

```text
chat                普通提问
document_summary    文档总结
markdown_generate   Markdown 生成
todo_extract        待办提取
```

---

# 8. ai_messages 表

V1 可选，建议预建表。

```sql
CREATE TABLE ai_messages (
    id                BIGSERIAL PRIMARY KEY,
    conversation_id   BIGINT NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    role              VARCHAR(16) NOT NULL,
    content           TEXT NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_messages_conversation_id ON ai_messages(conversation_id);
```

### role 枚举

```text
system
user
assistant
```

---

# 9. ai_call_logs 表

V1 可选，用于排查问题和统计。

```sql
CREATE TABLE ai_call_logs (
    id                 BIGSERIAL PRIMARY KEY,
    owner_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_config_id BIGINT REFERENCES ai_provider_configs(id) ON DELETE SET NULL,
    feature_type       VARCHAR(32),
    model_name         VARCHAR(128),
    success            BOOLEAN NOT NULL DEFAULT FALSE,
    error_message      TEXT,
    latency_ms         INT,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_call_logs_owner_id ON ai_call_logs(owner_id);
```

### 安全要求

```text
ai_call_logs 禁止保存：
- prompt 内容
- API Key
- 完整请求体
只保存状态信息和性能指标。
```

---

# 10. 实体关系总览

```text
users 1 ─── N documents
users 1 ─── N todos
users 1 ─── N ai_provider_configs
users 1 ─── N ai_conversations
users 1 ─── N ai_call_logs
ai_conversations 1 ─── N ai_messages
documents N ─── 1 ai_conversations（可选，来源关联）
todos N ─── 1 documents（可选，来源文档）
```

---

# 11. 索引策略

| 表 | 索引字段 | 原因 |
|---|---|---|
| documents | owner_id | 查询我的文档 |
| documents | is_public | 查询公开文档 |
| documents | updated_at DESC | 按更新时间排序 |
| todos | owner_id | 查询我的待办 |
| todos | completed | 过滤未完成/已完成 |
| ai_provider_configs | owner_id | 查询我的 Provider |
| ai_conversations | owner_id | 查询我的对话 |
| ai_messages | conversation_id | 查询某个对话的消息 |
| ai_call_logs | owner_id | 查询我的日志 |

---

# 12. 迁移策略

## V1 开发阶段

```yaml
spring.jpa.hibernate.ddl-auto: update
```

自动建表，方便开发。

## V1 上线前

切换为：

```yaml
spring.jpa.hibernate.ddl-auto: validate
```

配合 Flyway 或 Liquibase 做数据库版本管理。

## Flyway 文件命名约定

```text
V1__init_schema.sql           初始建表
V2__add_todos.sql             新增 todos
V3__add_ai_provider.sql       新增 AI Provider
V4__add_ai_conversation.sql   新增 AI 对话
```

---

# 13. 当前结论

MKM V1 数据库设计完成，共 7 张表：

| 表 | V1 必建 |
|---|---|
| users | 是 |
| documents | 是 |
| todos | 是 |
| ai_provider_configs | 是 |
| ai_conversations | 建议预建 |
| ai_messages | 建议预建 |
| ai_call_logs | 可选 |

这套设计可以支撑 V1 所有功能，并为 V2/V3 的知识库、RAG、评论、社区功能预留扩展空间，不需要推翻重建。
