# 🎬 LLM 电影推荐系统

基于大语言模型（LLM）的智能电影推荐系统，采用 RAG（检索增强生成）架构，支持自然语言对话式电影推荐。

[![Java](https://img.shields.io/badge/Java-17-blue)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ✨ 核心特性

- 🤖 **AI 对话推荐** — 通过自然语言对话获取个性化电影推荐
- 🔍 **RAG 架构** — 结合向量检索与大语言模型，提供精准推荐
- 📚 **多轮对话** — 支持上下文感知的连续对话
- ❤️ **收藏管理** — 保存喜欢的电影，构建个人片单
- 🌐 **流式响应** — SSE 实时推送，告别等待
- 🔐 **JWT 认证** — 安全的用户身份验证

---

## 🏗️ 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.12 | Web 框架 |
| Spring AI | 1.0.3 | AI 集成框架 |
| MyBatis | 3.0.3 | ORM 框架 |
| MySQL | 8.0+ | 业务数据库 |
| Redis Stack | 7.x | 向量索引 + 缓存 |
| DeepSeek-V3 | - | 大语言模型 |
| Qwen3-Embedding | - | 向量嵌入模型 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.5+ | 前端框架 |
| Vite | 7.3+ | 构建工具 |
| Element Plus | 2.13+ | UI 组件库 |
| Pinia | 3.0+ | 状态管理 |
| Axios | 1.13+ | HTTP 客户端 |

---

## 📁 项目结构

```
LLM-Movie-recommendation-system/
├── movie-recommender/           # 后端项目
│   ├── src/main/java/
│   │   └── com/example/movierecommender/
│   │       ├── config/          # 配置类
│   │       ├── controller/      # REST API 控制器
│   │       ├── service/         # 业务逻辑层
│   │       ├── mapper/          # MyBatis Mapper
│   │       ├── entity/          # 数据实体
│   │       ├── dto/             # 数据传输对象
│   │       ├── security/        # JWT 安全组件
│   │       └── common/          # 通用工具类
│   ├── src/main/resources/      # 配置文件、SQL
│   └── pom.xml                  # Maven 配置
├── movie-recommender/frontend/  # 前端项目
│   ├── src/
│   │   ├── views/               # 页面视图
│   │   ├── components/          # 组件
│   │   ├── api/                 # API 封装
│   │   ├── stores/              # Pinia 状态
│   │   └── router/              # 路由配置
│   ├── package.json
│   └── vite.config.js
└── docs/                        # 设计文档
```

---

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis Stack 7.x

### 1. 克隆项目

```bash
git clone https://github.com/NvidiaJensenHuang/LLM-Movie-recommendation-system.git
cd LLM-Movie-recommendation-system
```

### 2. 配置环境变量

复制 `.env.example` 为 `.env`，并填写你的 API 密钥：

```bash
cp .env.example .env
```

编辑 `.env` 文件：

```env
# DeepSeek API (用于大语言模型)
DEEPSEEK_API_KEY=REDACTED

# DashScope API (用于向量嵌入)
DASHSCOPE_API_KEY=REDACTED

# TMDB API (用于电影数据采集)
TMDB_API_KEY=REDACTED

# MySQL 密码
MYSQL_PASSWORD=REDACTED

# JWT 密钥
JWT_SECRET=REDACTED
```

### 3. 初始化数据库

```sql
-- 创建数据库
CREATE DATABASE movie_recommender CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行 schema.sql 创建表结构
-- 位于 movie-recommender/src/main/resources/schema.sql
```

### 4. 启动后端服务

```bash
cd movie-recommender
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 5. 启动前端服务

```bash
cd movie-recommender/frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

---

## 📡 API 接口

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |

### 用户接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/profile` | 获取个人信息 |
| PUT | `/api/user/profile` | 修改个人信息 |

### 对话接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/chat/stream` | SSE 流式对话 |
| POST | `/api/conversations` | 创建新会话 |
| GET | `/api/conversations` | 获取会话列表 |
| DELETE | `/api/conversations/{id}` | 删除会话 |
| GET | `/api/conversations/{id}/messages` | 获取会话消息 |

### 电影接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/movies/{id}` | 获取电影详情 |
| GET | `/api/movies/search` | 搜索电影 |

### 收藏接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/favorites` | 添加收藏 |
| DELETE | `/api/favorites/{movieId}` | 取消收藏 |
| GET | `/api/favorites` | 获取收藏列表 |

---

## 🔧 RAG 推荐流程

```
用户输入 → Embedding → 向量检索 → Prompt构建 → LLM生成 → 流式输出
                ↓              ↓
          Qwen3嵌入      Redis向量库
```

1. **用户输入** — 自然语言描述观影需求
2. **Embedding** — Qwen3-Embedding 将查询转换为向量
3. **向量检索** — 在 Redis Vector Store 中搜索相似电影
4. **Prompt构建** — 将检索结果注入 System Prompt
5. **LLM生成** — DeepSeek-V3 生成个性化推荐
6. **流式输出** — 通过 SSE 实时返回给前端

---

## 📝 系统提示词

```
你是一个专业的电影推荐助手。请仅根据以下电影信息回答用户问题。
如果提供的电影中没有相关内容，请诚实地说"抱歉，我没有找到相关电影"。

## 可供推荐的电影：
{context}

请用 Markdown 格式回复，每部推荐的电影包含：标题、评分、类型、上映年份、推荐理由。
```

---

## 🎨 功能截图

> 待添加

---

## 🛠️ 开发计划

- [x] 基础架构搭建
- [x] 用户认证系统
- [x] RAG 推荐核心
- [x] 对话管理
- [x] 电影收藏
- [ ] 管理后台
- [ ] 推荐历史分析
- [ ] 多语言支持
- [ ] 移动端适配

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 📄 许可证

本项目采用 [MIT](LICENSE) 许可证开源。

---

## 🙏 致谢

- [DeepSeek](https://deepseek.com/) - 提供大语言模型支持
- [通义千问](https://tongyi.aliyun.com/) - 提供向量嵌入服务
- [TMDB](https://www.themoviedb.org/) - 提供电影数据
- [Spring AI](https://spring.io/projects/spring-ai) - AI 应用框架

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/NvidiaJensenHuang">NvidiaJensenHuang</a>
</p>
