# MKM - Markdown Knowledge Manager

一个面向程序员的 Markdown 知识管理 + AI 助手学习项目。

## 项目模块

- `backend/`：Spring Boot (Kotlin) 后端服务，提供认证、文档管理 API
- `android/`：Android App，Kotlin + XML，支持 Markdown 查看
- `web/`：Vue 3 + Vite 网页端，支持登录、文档列表、Markdown 渲染

## V1 目标

1. 用户注册 / 登录
2. 创建、查看、编辑、删除 Markdown 文档
3. Android 端渲染 Markdown
4. Web 端渲染 Markdown
5. PostgreSQL 持久化存储

## 快速启动

### 启动数据库

```bash
docker compose up -d db
```

### 启动后端

```bash
cd backend
./gradlew bootRun
```

如果本地没有 Gradle Wrapper，可先使用本机 Gradle：

```bash
gradle bootRun
```

### 启动 Web

```bash
cd web
npm install
npm run dev
```

### Android 调试

Android 模拟器访问宿主机后端使用：

```text
http://10.0.2.2:8080/api/
```

真机调试时请把 `RetrofitClient.BASE_URL` 改为电脑局域网 IP。

## 后续规划

- V2：AI 助手，总结 Markdown、生成 Markdown、创建待办
- V3：公开知识库、评论、CLI 给其他 AI Agent 调用
