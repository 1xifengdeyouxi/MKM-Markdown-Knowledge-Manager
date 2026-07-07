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
| documents | Markdown 文档（含文件夹路径、同步状态） |
| attachments | 附件（二进制 + 元数据） |
| todos | 待办任务 |
| ai_provider_configs | 用户 AI 模型配置 |
| ai_conversations | AI 对话会话 |
| ai_messages | AI 对话消息 |
| ai_call_logs | AI 调用日志（仅状态/性能） |

V2/V3 扩展表（本文档不实现，仅预留设计方向）：

| 表名 | 阶段 | 说明 |
|---|---|---|
| comments | V3 | 评论 |
| likes | V3 | 点赞 |
| document_tags | V2 | 标签升级 |
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
    nickname     VARCHAR(64),
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
| avatar_url | VARCHAR(512) | 头像 URL |
| nickname | VARCHAR(64) | 显示昵称 |
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
    @Column(length = 64)
    var nickname: String? = null,
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
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(256) NOT NULL,
    content          TEXT NOT NULL DEFAULT '',
    folder_path      VARCHAR(1024) NOT NULL DEFAULT '/',
    file_name        VARCHAR(256) NOT NULL,
    tags             VARCHAR(512),
    sync_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    local_updated_at TIMESTAMP,
    owner_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_owner_id ON documents(owner_id);
CREATE INDEX idx_documents_folder_path ON documents(owner_id, folder_path);
CREATE INDEX idx_documents_updated_at ON documents(updated_at DESC);
```

### 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| title | VARCHAR(256) | 文档标题，必填 |
| content | TEXT | Markdown 内容 |
| folder_path | VARCHAR(1024) | 文件夹路径，如 `/Android/` |
| file_name | VARCHAR(256) | 文件名，如 `Room.md` |
| tags | VARCHAR(512) | 标签，逗号分隔 |
| sync_enabled | BOOLEAN | 是否开启云端同步 |
| local_updated_at | TIMESTAMP | 本地最后修改时间，用于冲突检测 |
| owner_id | BIGINT | 所属用户 FK |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 云端更新时间 |

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
    @Column(nullable = false, length = 1024)
    var folderPath: String = "/",
    @Column(nullable = false, length = 256)
    var fileName: String,
    @Column(length = 512)
    var tags: String? = null,
    @Column(nullable = false)
    var syncEnabled: Boolean = false,
    var localUpdatedAt: LocalDateTime? = null,
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

# 5. attachments 表

```sql
CREATE TABLE attachments (
    id            BIGSERIAL PRIMARY KEY,
    document_id   BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    owner_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name     VARCHAR(256) NOT NULL,
    relative_path VARCHAR(1024) NOT NULL,
    mime_type     VARCHAR(128),
    file_size     BIGINT NOT NULL,
    data          BYTEA NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attachments_document_id ON attachments(document_id);
CREATE INDEX idx_attachments_owner_id ON attachments(owner_id);
```

### 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| document_id | BIGINT | 关联文档 FK |
| owner_id | BIGINT | 所属用户 FK |
| file_name | VARCHAR(256) | 原始文件名 |
| relative_path | VARCHAR(1024) | 相对于知识库根目录的路径 |
| mime_type | VARCHAR(128) | 文件 MIME 类型 |
| file_size | BIGINT | 文件大小（字节），上限 10MB |
| data | BYTEA | 附件二进制数据 |
| created_at | TIMESTAMP | 上传时间 |

### JPA 实体

```kotlin
@Entity @Table(name = "attachments")
class Attachment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    val document: Document,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,
    @Column(nullable = false, length = 256)
    val fileName: String,
    @Column(nullable = false, length = 1024)
    val relativePath: String,
    @Column(length = 128)
    val mimeType: String? = null,
    @Column(nullable = false)
    val fileSize: Long,
    @Column(nullable = false, columnDefinition = "BYTEA")
    val data: ByteArray,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

---

# 6. todos 表

```sql
CREATE TABLE todos (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(256) NOT NULL,
    note                TEXT,
    completed           BOOLEAN NOT NULL DEFAULT FALSE,
    priority            VARCHAR(16) DEFAULT 'medium',
    due_date            TIMESTAMP,
    source_type         VARCHAR(32) DEFAULT 'manual',
    source_document_id  BIGINT REFERENCES documents(id) ON DELETE SET NULL,
    source_document_title VARCHAR(256),
    completed_at        TIMESTAMP,
    owner_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_todos_owner_id ON todos(owner_id);
CREATE INDEX idx_todos_completed ON todos(completed);
CREATE INDEX idx_todos_due_date ON todos(due_date);
```

### 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| title | VARCHAR(256) | 待办标题，必填 |
| note | TEXT | 备注，可选 |
| completed | BOOLEAN | 是否完成 |
| priority | VARCHAR(16) | low / medium / high |
| due_date | TIMESTAMP | 截止时间，可选 |
| source_type | VARCHAR(32) | manual / ai |
| source_document_id | BIGINT | 来源文档 FK，可选 |
| source_document_title | VARCHAR(256) | 来源文档标题（冗余，防 FK 丢失） |
| completed_at | TIMESTAMP | 完成时间 |
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
    var note: String? = null,
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
    @Column(length = 256)
    var sourceDocumentTitle: String? = null,
    var completedAt: LocalDateTime? = null,
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

# 7. ai_provider_configs 表

```sql
CREATE TABLE ai_provider_configs (
    id                BIGSERIAL PRIMARY KEY,
    owner_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_name     VARCHAR(64) NOT NULL,
    base_url          VARCHAR(512) NOT NULL,
    encrypted_api_key TEXT NOT NULL,
    model_name        VARCHAR(128) NOT NULL,
    enabled           BOOLEAN NOT NULL DEFAULT TRUE,
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
| base_url | VARCHAR(512) | OpenAI 兼容 API Base URL |
| encrypted_api_key | TEXT | 加密后的 API Key |
| model_name | VARCHAR(128) | 模型名，如 deepseek-chat |
| enabled | BOOLEAN | 是否启用 |
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
    @Column(nullable = false, length = 512)
    var baseUrl: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    var encryptedApiKey: String,
    @Column(nullable = false, length = 128)
    var modelName: String,
    @Column(nullable = false)
    var enabled: Boolean = true,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate fun onUpdate() { updatedAt = LocalDateTime.now() }
}
```

---

# 8. ai_conversations 表

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

### JPA 实体

```kotlin
@Entity @Table(name = "ai_conversations")
class AiConversation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,
    @Column(nullable = false, length = 256)
    var title: String = "新对话",
    @Column(length = 32)
    var sourceType: String = "chat",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id")
    var sourceDocument: Document? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_config_id")
    var providerConfig: AiProviderConfig? = null,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate fun onUpdate() { updatedAt = LocalDateTime.now() }
}
```

---

# 9. ai_messages 表

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

# 10. ai_call_logs 表

用于排查问题和统计，禁止保存敏感内容。

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
- 完整响应体
只保存状态信息和性能指标。
```

---

# 11. 实体关系总览

```text
users 1 ─── N documents
users 1 ─── N attachments
users 1 ─── N todos
users 1 ─── N ai_provider_configs
users 1 ─── N ai_conversations
users 1 ─── N ai_call_logs
documents 1 ─── N attachments
ai_conversations 1 ─── N ai_messages
todos N ─── 1 documents（可选，来源文档）
```

---

# 12. 索引策略

| 表 | 索引字段 | 原因 |
|---|---|---|
| documents | owner_id | 查询我的文档 |
| documents | (owner_id, folder_path) | 查询某文件夹文档 |
| documents | updated_at DESC | 按更新时间排序 |
| attachments | document_id | 查询某文档的附件 |
| attachments | owner_id | 查询用户附件 |
| todos | owner_id | 查询我的待办 |
| todos | completed | 过滤未完成/已完成 |
| todos | due_date | 按截止时间排序 |
| ai_provider_configs | owner_id | 查询我的 Provider |
| ai_conversations | owner_id | 查询我的对话 |
| ai_messages | conversation_id | 查询某个对话的消息 |
| ai_call_logs | owner_id | 查询我的日志 |

---

# 13. 迁移策略

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

配合 Flyway 做数据库版本管理。

## Flyway 文件命名约定

```text
V1__init_schema.sql           初始建表（users, documents, attachments, todos）
V2__add_ai_tables.sql         添加 AI 相关表
V3__add_ai_call_logs.sql      添加 AI 调用日志
```

---

# 14. 当前结论

MKM V1 数据库设计共 8 张表：

| 表 | V1 必建 |
|---|---|
| users | 是 |
| documents | 是（含文件夹路径、同步状态） |
| attachments | 是（PostgreSQL 二进制存储，10MB 上限） |
| todos | 是 |
| ai_provider_configs | 是 |
| ai_conversations | 是 |
| ai_messages | 是 |
| ai_call_logs | 建议预建 |

这套设计可以支撑 V1 所有功能，并为 V2/V3 预留扩展空间，不需要推翻重建。
