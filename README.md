# Online Exam

一个前后端分离的在线考试系统，包含教师端组卷与阅卷、学生端考试与成绩查询、用户注册登录等功能。

## 项目结构

```text
online_exam/
├─ online_exam/       # Spring Boot 后端（Java 17）
├─ online-exam-web/   # Vue 3 + Vite 前端
└─ database/          # MySQL 建表脚本
```

## 技术栈

- 后端：Spring Boot 3、Spring MVC、Spring Data JPA、Thymeleaf、MySQL
- 前端：Vue 3、Vue Router、Element Plus、Axios、Vite
- 工程化：Maven Wrapper、npm

## 主要功能

- 用户注册、登录与角色区分（教师 / 学生）
- 教师创建考试、维护题目、设置考试时间与提交策略
- 学生参加考试、提交答案、查看考试记录与成绩
- 教师查看提交记录与答卷详情

## 本地运行

### 1. 初始化数据库

使用 MySQL 创建 `online_exam` 数据库，然后按依赖顺序执行 `database/` 目录中的 SQL 文件。示例：

```sql
CREATE DATABASE online_exam DEFAULT CHARACTER SET utf8mb4;
```

建议按以下顺序执行：`online_exam_users.sql`、`online_exam_exam.sql`、`online_exam_question.sql`、`online_exam_option_item.sql`、`online_exam_exam_question.sql`、`online_exam_student_exam.sql`、`online_exam_student_answer.sql`。

### 2. 配置后端数据库连接

后端从环境变量读取数据库账号和密码。在 PowerShell 中运行：

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "你的数据库密码"
```

### 3. 启动后端

```powershell
cd online_exam
./mvnw.cmd spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 4. 启动前端

另开一个终端：

```powershell
cd online-exam-web
npm install
npm run dev
```

前端开发服务器会通过 Vite 代理访问后端 `/api` 接口。

