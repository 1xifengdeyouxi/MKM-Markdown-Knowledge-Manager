# MKM - Markdown Knowledge Manager

MKM 是一个面向程序员的 Markdown 知识管理学习项目，包含 PostgreSQL 数据库、Spring Boot Kotlin 后端、Vue 3 Web 端和 Android 端。

## 项目模块

- `backend/`：Spring Boot 3 + Kotlin 后端，提供用户认证和文档管理 API。
- `web/`：Vue 3 + Vite Web 端，支持注册、登录、文档列表和 Markdown 渲染。
- `android/`：Android App，Kotlin + XML，支持注册、登录和 Markdown 查看。
- `docker-compose.yml`：本地 PostgreSQL 数据库配置。

## 本地启动顺序

请按顺序启动：数据库 → 后端 → Web 或 Android。

### 1. 启动数据库

项目根目录执行：

```bash
cd /Users/windmeta/Desktop/Mytest/myProject
docker compose up db -d
```

确认数据库容器运行：

```bash
docker ps
```

看到 `mkm-postgres` 且状态为 `Up` 即可。数据库默认配置：

- 地址：`localhost:5432`
- 数据库：`mkm_db`
- 用户名：`mkm`
- 密码：`mkm`

停止数据库：

```bash
docker compose down
```

如果要同时删除数据库数据卷，执行：

```bash
docker compose down -v
```

### 2. 启动后端

新开一个终端，执行：

```bash
cd /Users/windmeta/Desktop/Mytest/myProject/backend
./gradlew bootRun
```

看到类似日志表示后端启动成功：

```text
Tomcat started on port 8080 (http)
Started MkmApplicationKt
```

后端地址：

```text
http://localhost:8080
```

说明：`bootRun` 是长期运行任务，启动成功后 Gradle 进度可能停在 `85% EXECUTING`，这是正常的。需要停止后端时，在该终端按 `Ctrl + C`。

### 3. 启动 Web 前端

保持数据库和后端运行，再新开一个终端执行：

```bash
cd /Users/windmeta/Desktop/Mytest/myProject/web
npm install
npm run dev
```

Web 默认地址：

```text
http://localhost:5173
```

Web 的接口请求走 Vite 代理：

```text
前端 /api -> 后端 http://localhost:8080/api
```

### 4. 启动 Android 端

保持数据库和后端运行，然后用 Android Studio 打开：

```text
/Users/windmeta/Desktop/Mytest/myProject/android
```

等待 Gradle Sync 完成后，选择模拟器或真机运行 `app`。

Android 模拟器访问电脑本机后端时，项目已配置为：

```text
http://10.0.2.2:8080/api/
```

对应文件：

```text
android/app/src/main/java/com/mkm/android/data/remote/RetrofitClient.kt
```

如果使用真机调试，`10.0.2.2` 不能访问电脑，需要把 `BASE_URL` 改成电脑的局域网 IP，例如：

```kotlin
private const val BASE_URL = "http://192.168.1.100:8080/api/"
```

电脑和手机必须连接同一个局域网，并确保 macOS 防火墙没有拦截 8080 端口。

## 怎么登录

当前项目没有预置测试账号，需要先注册账号，再登录。

### Web 端登录

1. 打开 `http://localhost:5173`。
2. 在登录页输入任意用户名和密码。
3. 点击注册按钮创建账号。
4. 注册成功后会自动保存登录状态；之后也可以用同一用户名和密码点击登录。

### Android 端登录

1. 启动 Android App。
2. 在登录页输入任意用户名和密码。
3. 点击注册按钮创建账号。
4. 注册成功后进入主页面；之后也可以用同一用户名和密码登录。

### 用命令行测试注册和登录

注册：

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"test","password":"123456"}'
```

登录：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"test","password":"123456"}'
```

成功后会返回类似结果：

```json
{
  "token": "...",
  "username": "test"
}
```

## 常见问题

### Docker 拉取 PostgreSQL 镜像超时

如果出现 `failed to fetch anonymous token` 或 `context deadline exceeded`，通常是 Docker Hub 网络超时。可以在 Docker Desktop 的 Docker Engine 配置里添加镜像源，然后 Apply & Restart。

### 后端启动后一直显示 85%

这是正常的。`./gradlew bootRun` 启动的是 Web 服务，服务会一直运行并监听 8080 端口，不会自动退出。

### Android 编译提示 AndroidX 未启用

项目需要启用 AndroidX，根目录 `android/gradle.properties` 应包含：

```properties
android.useAndroidX=true
android.enableJetifier=true
```

### Android 真机无法连接后端

真机不能使用 `10.0.2.2`，需要把 Android 的 `BASE_URL` 改成电脑局域网 IP，并保证手机和电脑在同一网络。

## 后续规划

- V2：AI 助手，总结 Markdown、生成 Markdown、创建待办。
- V3：公开知识库、评论、CLI 给其他 AI Agent 调用。
