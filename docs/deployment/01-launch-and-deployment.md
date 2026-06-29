# MKM 上线路径与部署方案

## 1. 文档目标

本文档定义 MKM 从本地学习项目逐步上线为真实产品的路径，包括：

- 本地开发环境
- 测试环境
- 生产环境
- Docker 部署方案
- 域名与 HTTPS
- 数据库与备份
- Android 发布
- Web 发布

---

## 2. 上线路径总览

MKM 推荐按 4 个阶段上线：

```text
Phase 1：本地开发可运行
Phase 2：个人服务器测试环境
Phase 3：小范围内测上线
Phase 4：正式产品化上线
```

---

# 3. Phase 1：本地开发环境

## 3.1 目标

完成本地完整闭环：

```text
Android App → 本地 Backend → 本地 PostgreSQL
Web → 本地 Backend → 本地 PostgreSQL
```

## 3.2 本地组件

| 组件 | 运行方式 |
|---|---|
| PostgreSQL | docker compose up db |
| Backend | ./gradlew bootRun |
| Web | npm run dev |
| Android | Android Studio Run |

## 3.3 本地配置

### Backend

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mkm_db
    username: mkm
    password: mkm
```

### Android 模拟器

```text
BASE_URL=http://10.0.2.2:8080/api/
```

### Web

```text
Vite Proxy: /api → http://localhost:8080
```

---

# 4. Phase 2：个人服务器测试环境

## 4.1 目标

将后端、数据库和 Web 部署到一台云服务器上，Android 真机可以访问。

## 4.2 推荐服务器配置

学习/内测阶段：

| 资源 | 建议 |
|---|---|
| CPU | 2 核 |
| 内存 | 2GB / 4GB |
| 磁盘 | 40GB SSD |
| 系统 | Ubuntu 22.04 LTS |
| 带宽 | 3-5 Mbps |

国内云可选：

- 阿里云
- 腾讯云
- 华为云
- 百度云
- 火山引擎

海外可选：

- DigitalOcean
- Hetzner
- Railway
- Render
- Fly.io

---

## 4.3 Docker Compose 测试环境

推荐结构：

```text
server/
├── docker-compose.yml
├── nginx/
│   └── default.conf
├── backend/
│   └── Dockerfile
└── web/
    └── Dockerfile
```

服务：

```text
nginx
backend
web
db
```

---

## 4.4 docker-compose.yml 示例

```yaml
services:
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: mkm_db
      POSTGRES_USER: mkm
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pg_data:/var/lib/postgresql/data
    restart: always

  backend:
    image: mkm-backend:latest
    environment:
      DB_USER: mkm
      DB_PASS: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      AI_KEY_ENCRYPTION_SECRET: ${AI_KEY_ENCRYPTION_SECRET}
    depends_on:
      - db
    restart: always

  web:
    image: mkm-web:latest
    restart: always

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
      - ./certbot:/etc/letsencrypt
    depends_on:
      - backend
      - web
    restart: always

volumes:
  pg_data:
```

---

# 5. Phase 3：小范围内测上线

## 5.1 目标

邀请 5-20 个技术朋友使用。

主要验证：

- 注册登录是否稳定
- Android Markdown 渲染是否好用
- 文档同步是否可靠
- AI Key 配置是否容易理解
- AI 总结和生成 Markdown 是否有价值
- 待办功能是否真实使用

## 5.2 内测准备

上线前需要完成：

- HTTPS
- 数据库备份
- 错误日志
- 基础隐私说明
- 用户 API Key 安全说明
- Android APK 安装包
- Web 访问地址

## 5.3 内测反馈表

建议收集：

| 问题 | 类型 |
|---|---|
| 你是否能成功注册登录？ | 可用性 |
| Markdown 查看体验如何？ | 核心体验 |
| AI 配置是否困难？ | AI |
| AI 总结是否有用？ | AI |
| 待办功能是否会用？ | 产品 |
| 你最希望增加什么？ | 需求 |
| 你觉得这个产品和 Obsidian 的差异是什么？ | 定位 |

---

# 6. Phase 4：正式产品化上线

## 6.1 正式上线前必须补齐

| 能力 | 说明 |
|---|---|
| 用户协议 | 说明产品责任和使用限制 |
| 隐私政策 | 说明用户数据、API Key、AI 请求数据处理 |
| 备份机制 | PostgreSQL 定时备份 |
| 日志系统 | 后端错误日志、AI 调用失败日志 |
| 监控 | 服务存活、磁盘、CPU、内存 |
| 限流 | 防止接口滥用 |
| 数据导出 | 用户可以导出 Markdown |
| 删除账号 | 用户可以删除自己的数据 |

---

## 6.2 推荐部署架构

```text
用户 Android / Web
        │
        ▼
    HTTPS / Nginx
        │
        ├── /api → Spring Boot Backend
        │
        └── / → Vue Web 静态资源
        │
        ▼
    PostgreSQL
```

AI 调用：

```text
Backend
  → 解密用户 API Key
  → 调用 DeepSeek / MiniMax / OpenAI 等 Provider
```

---

# 7. 域名与 HTTPS

## 7.1 域名建议

开发阶段：

```text
mkm.yourdomain.com
```

正式产品：

```text
mkm.app
mkmnotes.com
markdownkm.com
```

## 7.2 HTTPS

推荐使用：

```text
Nginx + Certbot + Let's Encrypt
```

证书自动续期：

```bash
certbot renew --dry-run
```

---

# 8. 数据库备份策略

## 8.1 内测阶段

每日备份一次：

```bash
pg_dump -U mkm mkm_db > backup_$(date +%Y%m%d).sql
```

保留最近 7 天。

## 8.2 正式阶段

建议：

- 每日全量备份
- 每小时增量或 WAL 归档（后期）
- 备份上传对象存储
- 定期恢复演练

---

# 9. Android 发布策略

## 9.1 内测阶段

优先发布 APK：

```text
mkm-v0.1.0-debug.apk
mkm-v0.1.0-internal.apk
```

渠道：

- 微信/QQ群内测
- GitHub Releases
- 网盘分享

## 9.2 正式阶段

国内上架可选：

- 应用宝
- 华为应用市场
- 小米应用商店
- OPPO / vivo
- 酷安

海外：

- Google Play

## 9.3 版本号规范

```text
0.1.0  内测 MVP
0.2.0  AI 增强
0.3.0  公开知识库
1.0.0  正式版
```

---

# 10. Web 发布策略

V1 Web 可以作为辅助端：

```text
https://mkm.example.com
```

部署方式：

- Vue build 后交给 Nginx 静态托管
- `/api` 反向代理到 Spring Boot

```nginx
location / {
    root /usr/share/nginx/html;
    try_files $uri $uri/ /index.html;
}

location /api/ {
    proxy_pass http://backend:8080/api/;
}
```

---

# 11. 环境变量清单

生产环境必须配置：

```text
DB_PASSWORD=强密码
JWT_SECRET=至少32位随机字符串
AI_KEY_ENCRYPTION_SECRET=至少32位随机字符串
SPRING_PROFILES_ACTIVE=prod
```

不要提交到 Git：

```text
.env
application-prod.yml 中的真实密钥
```

---

# 12. 安全注意事项

## 12.1 API Key 安全

- 后端加密保存
- 日志不打印
- 前端不回显完整 Key
- 用户可删除
- AI 调用失败时不要把 Key 带入错误信息

## 12.2 用户数据

- 文档默认私有
- 公开必须用户主动开启
- 删除文档要真正删除或提供回收站策略
- 用户应能导出自己的 Markdown

## 12.3 接口安全

- JWT 过期机制
- 所有资源必须校验 owner
- AI 接口限流
- 注册接口防刷
- 上传图片后期需校验文件类型和大小

---

# 13. 推荐上线里程碑

## Milestone 1：本地 Demo

目标：自己能完整使用。

```text
注册登录
文档 CRUD
Android 渲染
Web 同步
```

## Milestone 2：AI 可用版

目标：自己能用 AI 生成 Markdown 和待办。

```text
AI Provider 配置
普通提问
文档总结
Markdown 生成
待办提取
```

## Milestone 3：朋友内测版

目标：5-20 人试用。

```text
部署到服务器
HTTPS
APK 包
反馈收集
基本日志
```

## Milestone 4：公开试用版

目标：公开分享。

```text
官网介绍页
隐私政策
用户协议
数据备份
导出功能
```

---

# 14. 当前结论

MKM 推荐上线策略：

```text
先本地跑通 → 单服务器 Docker 部署 → 朋友内测 → 补齐隐私和备份 → 公开试用
```

V1 不需要一开始就上复杂云原生架构，一台 2C4G 云服务器 + Docker Compose + PostgreSQL + Nginx 足够支撑早期验证。

正式产品化前必须重点补齐：

```text
HTTPS
数据库备份
API Key 安全
用户数据导出
隐私政策
接口限流
错误日志
```
