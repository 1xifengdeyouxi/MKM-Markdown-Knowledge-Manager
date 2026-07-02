# MKM Web 端规范说明

## 1. 文档目标

本文档定义 MKM Web 端的职责范围、技术约束、页面规范、状态管理和验收标准。

Web 端是 MKM 的辅助管理端，不是 V1 的主体验端。

---

# 2. Web 端定位

```text
Web 端是 Android 主端的辅助工具。
主要功能：文档管理和阅读，账号登录，云同步验证。
不是 Web 优先产品。
```

V1 Web 端能力范围：

```text
登录 / 注册
文档列表
创建 / 编辑 / 预览文档
删除文档
Markdown 渲染
导航栏
基础响应式布局
```

V1 暂不做：

```text
Todo Web 端
AI 助手 Web 端
知识库 Web 端
个人设置 Web 端
评论 Web 端
```

---

# 3. 技术栈规范

## 3.1 必选技术

```text
Vue 3
Vite
Pinia
Vue Router 4
Axios
marked
DOMPurify
```

---

## 3.2 版本约束

| 依赖 | 版本 |
|---|---|
| Vue | 3.5.x |
| Vite | 5.x |
| Pinia | 2.2.x |
| Vue Router | 4.4.x |
| Axios | 1.x |
| marked | 14.x |
| DOMPurify | 3.x |

---

## 3.3 开发环境建议

| 项目 | 推荐版本 / 工具 | 说明 |
|---|---|---|
| Node.js | **20 LTS** | 推荐通过 nvm 管理版本 |
| npm | **10.x** | 随 Node 20 安装 |
| IDE | VS Code | 安装 Vue - Official 扩展 |
| 浏览器 | Chrome + Vue Devtools | 调试组件和 Pinia 状态 |

**安装 nvm（Apple Silicon Mac）：**

```sh
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
source ~/.zshrc
nvm install 20
nvm use 20
```

**首次安装依赖：**

```sh
cd web
npm install
npm run dev    # http://localhost:5173
```

详细安装说明见：`docs/deployment/00-dev-environment.md`。

---

## 3.4 已锁定依赖版本

| 依赖 | 版本（package.json） |
|---|---|
| vue | ^3.5.3 |
| vue-router | ^4.4.3 |
| pinia | ^2.2.2 |
| axios | ^1.7.7 |
| marked | ^14.1.2 |
| dompurify | ^3.1.6 |
| vite | ^5.4.2 |
| @vitejs/plugin-vue | ^5.1.2 |

---

## 3.5 不使用

```text
TypeScript（V1 使用 JavaScript）
Element Plus / Ant Design Vue（V1 使用原生 CSS + 少量组件）
SSR / Nuxt（V1 纯 CSR）
```

---

# 4. 项目结构规范

```text
web/src/
├── api/
│   ├── client.js           Axios 实例 + 拦截器
│   ├── auth.js
│   ├── document.js
│   └── user.js
├── stores/
│   ├── auth.js             登录状态
│   └── document.js         文档状态
├── router/
│   └── index.js
├── views/
│   ├── LoginView.vue
│   ├── DocumentListView.vue
│   └── DocumentDetailView.vue
├── components/
│   ├── NavBar.vue
│   ├── DocumentCard.vue
│   └── MarkdownRenderer.vue
├── App.vue
└── main.js
```

---

# 5. 路由规范

```text
/login                 登录/注册页
/                      文档列表（首页）
/documents/new         新建文档
/documents/:id         文档详情（预览 + 编辑）
```

路由守卫：

```text
未登录访问 / 和 /documents/* → 跳转 /login
已登录访问 /login → 跳转 /
```

---

# 6. 页面规范

## 6.1 LoginView

功能：

```text
登录 / 注册 Tab 切换
用户名输入
密码输入
错误提示
成功后跳转文档列表
```

---

## 6.2 DocumentListView

功能：

```text
文档卡片列表
新建文档按钮
点击卡片进入详情
退出登录
```

文档卡片信息：

```text
标题
更新时间
标签
```

---

## 6.3 DocumentDetailView

功能：

```text
预览模式：Markdown 渲染
编辑模式：标题输入 + 内容输入
模式切换按钮
保存按钮
删除文档
返回列表
```

---

## 6.4 MarkdownRenderer 组件

规范：

```text
使用 marked 渲染 Markdown
使用 DOMPurify 过滤 HTML，防止 XSS
支持 GFM（GitHub Flavored Markdown）
代码块加高亮支持（highlight.js，可选）
```

---

# 7. API 调用规范

## 7.1 Axios 实例

```text
Base URL：/api（通过 Vite proxy 转发到后端）
响应拦截器：统一处理 401 → 清除 Token → 跳 /login
请求拦截器：注入 Authorization: Bearer <token>
禁止日志打印 Token
```

---

## 7.2 Vite Proxy 配置

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

生产环境不使用 Vite Proxy，由 Nginx 反向代理处理。

---

# 8. 状态管理规范

## 8.1 auth store

```text
token
user
login()
register()
logout()
token 持久化：localStorage
```

---

## 8.2 document store

```text
documents
currentDocument
loading
error
fetchDocuments()
fetchDocument(id)
saveDocument(data)
deleteDocument(id)
```

---

# 9. 安全规范

Web 端必须满足：

```text
Token 存储在 localStorage
Markdown 渲染必须经过 DOMPurify 过滤
不回显密码
不日志打印 Token
401 自动跳转登录
生产环境使用 HTTPS
```

---

# 10. 样式规范

V1 使用简洁样式：

```text
无 UI 框架
原生 CSS + CSS Variables
移动优先响应式（最大宽度 960px）
字体：系统字体
代码字体：Menlo / Consolas / monospace
主色：#3F51B5（与 Android 保持一致）
背景：#FFFFFF / #F5F5F5
```

---

# 11. 部署规范

## 11.1 构建

```sh
npm run build
```

输出目录：`dist/`

---

## 11.2 Nginx 托管

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

# 12. Web 验收标准

V1 Web 端必须通过以下验证：

```text
打开 /login → 注册账号 → 跳转文档列表
文档列表展示正确
新建文档 → 输入内容 → 保存
点击文档 → 预览 Markdown 渲染
切换编辑模式 → 修改 → 保存
退出登录 → 跳转 /login
Android 创建的文档 → Web 刷新后能看到
```
