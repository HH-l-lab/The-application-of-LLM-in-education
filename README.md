[README.md](https://github.com/user-attachments/files/28008363/README.md)
# 基于大语言模型的实验教学智能辅助与自动评价平台

## 项目简介

本项目是一个基于 Spring Boot + Vue 的实验教学智能辅助与自动评价平台，集成了 DeepSeek 大语言模型和百度语音识别等 AI 能力，实现实验报告智能批改、语音转文字等功能。

## 线上测试

本项目已经通过阿里云服务器进行云端部署，可通过：http://39.102.119.198:81/ 来访问。

## 运行环境

| 开发环境项 | 系统名称/技术选型 | 版本/参数 |
|------|------|------|
| 编程语言 | Java(JDK) | 1.8.0_22 |
| 后端框架 | SpringBoot | 2.5.15 |
| 前端框架 | Vue.js/Element UI/EChart | 2.6.12/2.15.14/5.4.0 |
| 数据库 | MySQL/Redis | 8.0/6.2 |
| Web服务器 | Tomcat（Embeded） | 9.0.112 |
| 推理引擎 | DeepSeek LLM API | V4 Chat/Reasoning |
| 多媒体处理 | FFmpeg Toolset | 5.1 |
| 项目管理 | Apache Maven | 3.9.1 |
| 开发工具 | IntelliJ IDEA/VS Code |2023.2 Ultimate |
| 测试环境 | Google Chrome/Edge | 120.0.x |
| 部署服务器 | 阿里云云服务器 | 轻量2核2G-Ubuntu系统 |

## 数据库初始化

1. 创建 MySQL 数据库，数据库名为 `hh`，字符集设置为 `utf8mb4`。

```sql
CREATE DATABASE hh DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 导入 `sql/` 目录下的 SQL 脚本：

```bash
# 主数据库脚本
mysql -u root -p hh < sql/hh.sql
```

3. 修改后端数据库连接配置（按实际情况修改）：

文件路径：`henu-admin/src/main/resources/application-druid.yml`

```yaml
url: jdbc:mysql://localhost:3306/hh?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
username: hh
password: 123456
```

## 安装依赖

### 后端

```bash
# 在项目根目录执行
mvn clean install
```

### 前端

```bash
# 进入前端目录
cd henu-ui

# 安装依赖
npm install --registry=https://registry.npmmirror.com
```

## 启动命令

### 1. 启动 Redis

确保 Redis 服务已启动，默认端口 6379。

### 2. 启动后端

```bash
# 方式一：通过 Maven 运行
cd henu-admin
mvn spring-boot:run

# 方式二：在 IDE（IntelliJ IDEA）中运行
# 直接运行 henu-admin/src/main/java/com/henu/HenuApplication.java 的 main 方法
```

后端服务默认端口：`8080`

### 3. 启动前端

```bash
cd henu-ui
npm run dev
```

前端服务默认端口：`80`，浏览器访问 http://localhost:80

## 测试账号

| 角色 | 用户名 | 密码 |
|------|------|------|
| 管理员 | HHad | 123456 | 
| 创作者 | HH | 123456 | 
| 学生用户 | HHstu | 123456 | 

## 第三方 API 配置（可选）

如需使用 AI 相关功能，请在 `henu-admin/src/main/resources/application.yml` 中配置：

```yaml
ai:
  deepseek:
    api-key: "填入您的 DeepSeek API Key"
  baidu:
    app-id: "填入百度语音识别 AppID"
    api-key: "填入百度语音识别 API Key"
    secret-key: "填入百度语音识别 Secret Key"
```

## 项目结构

```
HenuGP
├── henu-admin        # 主启动模块（Web 层、Controller）
├── henu-common       # 通用工具模块（注解、工具类、常量）
├── henu-framework    # 框架核心模块（安全、拦截器、配置）
├── henu-system       # 系统业务模块（Service、Mapper）
├── henu-ui           # 前端项目（Vue 2 + Element UI）
├── sql               # 数据库初始化脚本
└── pom.xml           # Maven 父工程配置
```
