# MKM 开发环境配置说明

> 本文档记录 MKM 项目三端（Android / Backend / Web）所需的开发环境，包括推荐版本、安装方式、IDE 配置和常见问题排查。
>
> **平台：macOS (Apple Silicon / ARM64)**  
> **最后更新：2026-06-29**

---

## 快速检查清单

开始开发前，确认以下工具均已就绪：

```text
☐ JDK 17（Backend + Android 共用）
☐ Android Studio（含 Android SDK 34）
☐ Node.js 20 LTS（Web 端）
☐ Docker Desktop（运行 PostgreSQL）
☐ Git
```

---

## 1. Java / JDK

### 推荐版本：JDK 17

MKM 三端统一使用 **JDK 17 LTS**：

| 子项目 | JDK 要求 |
|---|---|
| Android | `compileOptions { JavaVersion.VERSION_17 }` |
| Backend | `java.toolchain { languageVersion = 17 }` |

> **注意**：Backend 的 `build.gradle.kts` 初始模板写的是 Java 21 toolchain，已更正为 17（见下方说明）。

### 当前状态（本机已满足）

```text
✅ JDK 17 已安装：/opt/homebrew/opt/openjdk@17
✅ JAVA_HOME 已指向 JDK 17
```

### 安装方式（如需重装）

```sh
brew install openjdk@17
```

然后在 `~/.zshrc` 中配置：

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

应用配置：

```sh
source ~/.zshrc
java -version  # 应输出 openjdk 17.x.x
```

### 注意：不要使用 JDK 21 运行后端

Backend 的 `build.gradle.kts` 中需确认 toolchain 为 17：

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)  // ← 确保是 17，不是 21
    }
}
```

---

## 2. Android 开发环境

### 2.1 Android Studio

| 项目 | 推荐 |
|---|---|
| Android Studio 版本 | **Meerkat \| 2024.3.1** 或更新 |
| Android Gradle Plugin | **8.3.2**（已在 `build.gradle.kts` 中锁定） |
| Gradle Wrapper | **8.7**（由 `gradle/wrapper/gradle-wrapper.properties` 管理） |
| Kotlin Plugin | **1.9.25**（已在 `build.gradle.kts` 中锁定） |
| KSP | **1.9.25-1.0.20**（Room 注解处理器） |

**下载地址**：https://developer.android.com/studio

### 2.2 Android SDK 组件

在 Android Studio → SDK Manager 中，确认以下组件已安装：

**SDK Platforms（需要）：**

```text
✅ Android 14.0 (API 34)  ← compileSdk / targetSdk（必须）
```

**SDK Build-Tools（需要）：**

```text
✅ Build-Tools 34.0.0
```

> 本机已有 API 34 和 Build-Tools 36+，均可正常构建。

**推荐安装的 SDK Tools：**

```text
☐ Android Emulator         模拟器
☐ Android Emulator Hypervisor Driver (Apple Silicon 专用)
☐ Android SDK Platform-Tools  adb 工具
```

### 2.3 模拟器配置

推荐创建以下 AVD（Android Virtual Device）：

| 设备 | API | ABI |
|---|---|---|
| Pixel 7 (推荐调试) | API 34 (Android 14) | arm64-v8a |

> Apple Silicon Mac 必须选 **arm64-v8a** 镜像，性能是 x86 的数倍。

创建步骤：Android Studio → Device Manager → Create Device → Pixel 7 → API 34 (arm64)

### 2.4 Android 项目配置概览

`android/app/build.gradle.kts` 关键配置：

```kotlin
compileSdk = 34
minSdk = 26        // Android 8.0+，覆盖约 97% 设备
targetSdk = 34
```

**核心依赖版本（已锁定）：**

| 依赖 | 版本 | 说明 |
|---|---|---|
| Kotlin Android | 1.9.25 | 与 KSP 版本必须匹配 |
| AGP (Android Gradle Plugin) | 8.3.2 | 构建系统 |
| KSP | 1.9.25-1.0.20 | Room 代码生成，替代 kapt |
| androidx.core:core-ktx | 1.13.1 | Kotlin 扩展 |
| material | 1.12.0 | Material Design 3 |
| navigation-fragment-ktx | 2.7.7 | Fragment 导航 |
| lifecycle-viewmodel-ktx | 2.8.4 | ViewModel |
| lifecycle-livedata-ktx | 2.8.4 | LiveData |
| activity-ktx | 1.9.1 | Activity 扩展 |
| fragment-ktx | 1.8.2 | Fragment 扩展 |
| kotlinx-coroutines-android | 1.8.1 | 协程 |
| retrofit2 | 2.11.0 | 网络请求 |
| converter-gson | 2.11.0 | JSON 序列化 |
| okhttp3 | 4.12.0 | HTTP 客户端 |
| logging-interceptor | 4.12.0 | 请求日志（仅 Debug） |
| room-runtime / room-ktx | 2.6.1 | 本地数据库 |
| room-compiler (KSP) | 2.6.1 | Room 代码生成 |
| markwon:core | 4.6.2 | Markdown 渲染核心 |
| markwon:ext-tables | 4.6.2 | GFM 表格支持 |
| markwon:ext-strikethrough | 4.6.2 | 删除线支持 |
| markwon:image | 4.6.2 | 图片渲染 |
| markwon:syntax-highlight | 4.6.2 | 代码高亮 |
| datastore-preferences | 1.1.1 | 轻量配置存储（替代 SharedPreferences） |

### 2.5 IDE 推荐配置

在 Android Studio 中建议开启：

- **File → Settings → Editor → Code Style → Kotlin** → 使用官方 Kotlin 代码风格
- **Build → Compiler** → 勾选 `Configure on demand`（加速增量构建）
- **Gradle JDK** → 确认指向 JDK 17（File → Project Structure → SDK Location → Gradle JDK）

---

## 3. Backend 开发环境

### 3.1 技术栈版本

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | **17** | 与 Android 共用 |
| Kotlin | **1.9.25** | 与 Android 端保持一致 |
| Spring Boot | **3.3.4** | 最新 3.3.x 稳定版 |
| Spring Security | **6.x**（随 Boot 3.3 自动管理） | |
| Gradle Wrapper | **8.8**（推荐，由 wrapper 管理） | |
| PostgreSQL | **16** (Docker 镜像 `postgres:16-alpine`) | |
| jjwt | **0.12.6** | JWT 生成和验证 |

### 3.2 IDE

推荐使用 **IntelliJ IDEA** 开发 Backend（Community 版即可）。

**IDEA 配置检查：**

1. File → Project Structure → SDK → 选 JDK 17
2. File → Settings → Build Tools → Gradle → Gradle JVM → 选 JDK 17
3. 安装插件（推荐）：
   - **Kotlin**（默认已内置）
   - **Spring Boot Assistant**（自动补全 `application.yml`）
   - **Database Navigator** 或 **DataGrip**（查看 PostgreSQL 表结构）

### 3.3 Backend 依赖概览

`backend/build.gradle.kts` 已配置的依赖（无需额外安装）：

```text
spring-boot-starter-web          REST API
spring-boot-starter-security     Spring Security 6
spring-boot-starter-data-jpa     JPA / Hibernate
spring-boot-starter-validation   参数校验 (@Valid)
jackson-module-kotlin            Kotlin 数据类 JSON 序列化
kotlin-reflect                   Kotlin 反射（Spring 必需）
jjwt-api:0.12.6                  JWT 核心接口
jjwt-impl:0.12.6 (runtimeOnly)   JWT 实现
jjwt-jackson:0.12.6 (runtimeOnly) JWT JSON 序列化
postgresql (runtimeOnly)         PostgreSQL JDBC 驱动
spring-boot-starter-test         测试框架
spring-security-test             Security 测试工具
```

### 3.4 PostgreSQL（通过 Docker 运行）

**不需要**在本机安装 PostgreSQL，使用 Docker Compose 启动数据库即可。

```sh
# 在项目根目录执行
docker compose up db -d
```

启动后连接信息：

```text
Host:     localhost
Port:     5432
Database: mkm_db
User:     mkm
Password: mkm（开发环境，生产必须换）
```

> 生产环境密码必须通过环境变量注入，不得使用 `mkm`。

### 3.5 Backend Gradle Wrapper

Backend 使用独立 Gradle Wrapper，版本为 **8.8**。正常开发启动后端时只使用 `./gradlew`，不要依赖全局 `gradle bootRun`。

如果首次拉取项目后 `backend/` 下缺少 `gradlew`，先临时安装系统 Gradle：

```sh
brew install gradle
```

然后在 `backend/` 目录生成 Wrapper。当前推荐使用国内镜像，避免访问 `services.gradle.org` 失败：

```sh
gradle wrapper --gradle-distribution-url https://mirrors.cloud.tencent.com/gradle/gradle-8.8-bin.zip
```

生成后确认以下文件存在，并提交到版本库：

```text
backend/gradlew
backend/gradlew.bat
backend/gradle/wrapper/gradle-wrapper.jar
backend/gradle/wrapper/gradle-wrapper.properties
```

### 3.6 Backend 开发启动命令

```sh
cd backend
./gradlew bootRun

# 或在 IDEA 中直接运行 MkmApplication.kt 的 main 函数
```

健康检查：

```sh
curl http://localhost:8080/api/actuator/health
```

---

## 4. Web 开发环境

### 4.1 Node.js 版本

| 项目 | 推荐 |
|---|---|
| Node.js | **20 LTS (Iron)** |
| npm | **10.x**（随 Node 20 安装） |

> 本机当前为 Node 26，超出推荐范围但兼容，开发不受影响。建议通过 `nvm` 管理多版本。

**安装 nvm（可选，多版本管理）：**

```sh
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
source ~/.zshrc
nvm install 20
nvm use 20
```

### 4.2 技术栈版本（已锁定在 package.json）

| 依赖 | 版本 | 说明 |
|---|---|---|
| vue | ^3.5.3 | Vue 3 核心 |
| vue-router | ^4.4.3 | 路由 |
| pinia | ^2.2.2 | 状态管理 |
| axios | ^1.7.7 | HTTP 客户端 |
| marked | ^14.1.2 | Markdown 渲染 |
| dompurify | ^3.1.6 | XSS 防护 |
| vite | ^5.4.2 | 构建工具（devDependency） |
| @vitejs/plugin-vue | ^5.1.2 | Vite Vue 插件 |

### 4.3 Web 开发启动命令

```sh
cd web
npm install          # 首次需安装依赖
npm run dev          # 开发服务器：http://localhost:5173
npm run build        # 生产构建，输出到 dist/
```

### 4.4 IDE

推荐使用 **VS Code** 开发 Web 端。

推荐安装扩展：

```text
Vue - Official (Vue Language Features)  Vue 3 语法支持
ESLint                                  代码检查
Prettier                                代码格式化
```

---

## 5. Docker（运行 PostgreSQL）

### 5.1 安装 Docker Desktop

Backend 的数据库通过 Docker 运行，需要安装 **Docker Desktop for Mac**。

**下载地址**：https://www.docker.com/products/docker-desktop/

选择 **Apple Silicon** 版本下载 `.dmg` 安装。

安装后验证：

```sh
docker --version           # 应输出 Docker version 27.x
docker compose version     # 应输出 Docker Compose version v2.x
```

### 5.2 启动开发数据库

```sh
# 在 myProject/ 根目录
docker compose up db -d

# 查看状态
docker compose ps

# 查看日志
docker compose logs db

# 停止
docker compose down
```

### 5.3 连接数据库（可视化工具）

推荐使用 **TablePlus** 或 **DBeaver** 连接 PostgreSQL：

```text
Type:     PostgreSQL
Host:     127.0.0.1
Port:     5432
Database: mkm_db
User:     mkm
Password: mkm
```

---

## 6. 推荐 IDE 组合

| 用途 | 推荐 IDE |
|---|---|
| Android 开发 | Android Studio (Meerkat 2024.3.1+) |
| Backend 开发 | IntelliJ IDEA (Community 或 Ultimate) |
| Web 开发 | VS Code |
| 数据库查看 | TablePlus / DBeaver |

> 可以只用 Android Studio 同时开发 Backend（它基于 IntelliJ 内核，对 Spring Boot Kotlin 有良好支持），但分开使用体验更好。

---

## 7. 项目初次启动步骤

按以下顺序启动，确保所有服务正常：

```sh
# Step 1: 启动数据库
cd /path/to/myProject
docker compose up db -d

# Step 2: 启动 Backend（等数据库就绪后）
cd backend
./gradlew bootRun
# 看到 "Started MkmApplication in X.XXX seconds" 表示启动成功

# Step 3: 启动 Web
cd ../web
npm install   # 首次执行
npm run dev
# 浏览器访问 http://localhost:5173

# Step 4: 运行 Android（用 Android Studio）
# 打开 android/ 目录 → 等待 Gradle 同步 → 选 Pixel 7 API 34 模拟器 → Run
```

---

## 8. 环境问题排查

### Q: Backend bootRun 报错 "Could not resolve JDK toolchain"

原因：Gradle 找不到 JDK 17。

解决：

```sh
# 确认 JAVA_HOME 指向 JDK 17
echo $JAVA_HOME
# /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# 在 IDEA 中：File → Settings → Gradle → Gradle JVM → 选 17
```

### Q: Backend 执行 `./gradlew bootRun` 报 `no such file or directory: ./gradlew`

原因：`backend/` 缺少 Gradle Wrapper 文件。

解决：

```sh
cd /path/to/myProject/backend
gradle wrapper --gradle-distribution-url https://mirrors.cloud.tencent.com/gradle/gradle-8.8-bin.zip
chmod +x gradlew
./gradlew bootRun
```

### Q: Backend 执行 `gradle wrapper --gradle-version 8.8` 报 `Test of distribution url ... failed`

原因：本机网络无法访问 Gradle 官方分发包 `services.gradle.org`。

解决：使用可访问的 Gradle 镜像地址生成 Wrapper：

```sh
gradle wrapper --gradle-distribution-url https://mirrors.cloud.tencent.com/gradle/gradle-8.8-bin.zip
```

### Q: Backend 执行 `gradle wrapper` 报 `Build was configured to prefer settings repositories over project repositories`

原因：`settings.gradle.kts` 已配置 `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)`，不允许在 `build.gradle.kts` 中再声明 `repositories { mavenCentral() }`。

解决：保留 `settings.gradle.kts` 中的仓库配置，删除 `backend/build.gradle.kts` 里的 `repositories` 块。

### Q: Android Gradle 同步失败，报 "Installed Build Tools revision X requires exactly revision Y"

解决：在 Android Studio → SDK Manager → SDK Tools → 安装对应 Build-Tools 版本（34.0.0）。

### Q: `docker compose up db` 报错 "Cannot connect to the Docker daemon"

原因：Docker Desktop 未启动。

解决：打开 Docker Desktop 应用等待启动完成（菜单栏出现鲸鱼图标），再执行命令。

### Q: Web `npm run dev` 访问 `/api` 接口报 404

原因：后端未启动，或 Vite proxy 未生效。

检查 `web/vite.config.js` 中 proxy 配置：

```js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

确认后端已在 `:8080` 正常运行。

### Q: Android 模拟器无法访问后端

模拟器访问宿主机使用特殊 IP，需在代码中正确配置 BASE_URL：

```text
模拟器访问宿主机：10.0.2.2:8080
真机调试（同局域网）：电脑的局域网 IP，如 192.168.1.x:8080
```

---

## 9. .gitignore 建议

各端需确认以下内容已被 `.gitignore` 忽略：

**Android：**
```gitignore
local.properties
*.keystore
```

**Backend：**
```gitignore
src/main/resources/application-dev.yml
src/main/resources/application-prod.yml
*.jar
build/
```

**Web：**
```gitignore
node_modules/
dist/
.env.local
```

**根目录：**
```gitignore
.DS_Store
.idea/
```
