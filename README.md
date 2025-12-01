
# 校园选课系统（微服务版）

## 项目简介

- **项目名称**: course-cloud
- **版本号**: v1.0.0
- **基于版本**: course:v1.1.0（hw04b 单体应用）
- **架构**: 微服务架构（初次拆分）

## 架构说明

### 系统架构图

```
客户端
│
├── catalog-service (8081) ───► catalog_db (3307)
│   └── 课程管理
│
└── enrollment-service (8082) ───► enrollment_db (3308)
├── 学生管理
├── 选课管理
└── HTTP调用 ───► catalog-service（验证课程）
```

### 微服务拆分

| 服务名称 | 端口 | 数据库 | 功能描述 |
|---------|------|--------|----------|
| catalog-service | 8081 | catalog_db | 课程目录服务，负责课程管理 |
| enrollment-service | 8082 | enrollment_db | 选课服务，负责学生管理和选课业务 |

### 服务间通信

- **通信方式**: HTTP REST API
- **调用方向**: enrollment-service → catalog-service
- **通信内容**: 课程验证、课程信息获取、选课人数更新

## 技术栈

- **后端框架**: Spring Boot 3.5.7
- **Java版本**: 17
- **数据库**: MySQL 8.4
- **容器化**: Docker & Docker Compose
- **服务通信**: RestTemplate
- **构建工具**: Maven 3.9+

## 环境要求

- **JDK**: 17+
- **Maven**: 3.8+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **MySQL**: 8.4（已容器化）

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/nykla23/course-cloud.git
cd course-cloud
```

### 2. 构建项目

```bash
# 构建所有服务
mvn clean package -DskipTests

# 或者分别构建
cd catalog-service && mvn clean package -DskipTests
cd ../enrollment-service && mvn clean package -DskipTests
```

### 3. 使用 Docker 部署

```bash
# 启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并清理数据
docker-compose down -v
```

### 4. 验证部署

```bash
# 运行测试脚本
chmod +x test-services.sh
./test-services.sh
```

## API 文档

### 课程目录服务 (catalog-service:8081)

#### 课程管理

| 方法 | 端点 | 描述         |
|------|------|------------|
| GET | `/api/courses` | 获取所有课程     |
| GET | `/api/courses/{id}` | 获取单个课程     |
| POST | `/api/courses` | 创建课程       |
| PUT | `/api/courses/{id}` | 更新课程       |
| DELETE | `/api/courses/{id}` | 删除课程       |
| GET | `/api/courses/instructor/{instructorId}` | 按讲师ID查询课程  |
| GET | `/api/courses/available` | 查询有剩余容量的课程 |

#### 课程创建示例

```bash
curl -X POST http://localhost:8081/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "courseCode": "CS101",
    "title": "计算机科学导论",
    "instructor": {
      "id": "T001",
      "name": "张教授",
      "email": "zhang@example.edu.cn"
    },
    "schedule": {
      "dayOfWeek": "MONDAY",
      "startTime": "08:00",
      "endTime": "10:00",
      "expectedAttendance": 50
    },
    "capacity": 60,
    "enrolledCount": 0
  }'
```

### 选课服务 (enrollment-service:8082)

#### 学生管理

| 方法     | 端点 | 描述      |
|--------|------|---------|
| GET    | `/api/students` | 获取所有学生  |
| GET    | `/api/students/{id}` | 获取单个学生  |
| POST   | `/api/students` | 创建学生    |
| PUT    | `/api/students/{id}` | 更新学生    |
| DELETE | `/api/students/{id}` | 删除学生    |
| GET    | `/api/students/grade/{grade}`   | 按年级查询学生 |

#### 选课管理

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/enrollments` | 获取所有选课记录 |
| POST | `/api/enrollments` | 学生选课 |
| DELETE | `/api/enrollments/{id}` | 学生退课 |
| GET | `/api/enrollments/course/{courseId}` | 按课程查询选课 |
| GET | `/api/enrollments/student/{studentId}` | 按学生查询选课 |

#### 学生创建示例

```bash
curl -X POST http://localhost:8082/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "2024001",
    "name": "张三",
    "major": "计算机科学与技术",
    "grade": 2024,
    "email": "zhangsan@example.edu.cn"
  }'
```

#### 选课示例

```bash
curl -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": "课程UUID",
    "studentId": "学生UUID"
  }'
```

## 数据库设计

### catalog_db (课程数据库)

```sql
CREATE TABLE courses (
    id VARCHAR(36) PRIMARY KEY,
    course_code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    capacity INT NOT NULL DEFAULT 0,
    enrolled_count INT NOT NULL DEFAULT 0,
    instructor_id VARCHAR(100),
    instructor_name VARCHAR(100),
    instructor_email VARCHAR(100),
    schedule_day VARCHAR(20),
    schedule_start_time VARCHAR(10),
    schedule_end_time VARCHAR(10),
    expected_attendance INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### enrollment_db (选课数据库)

```sql
CREATE TABLE students (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    major VARCHAR(100),
    grade INT,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE enrollments (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DROPPED', 'COMPLETED')),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_course_student (course_id, student_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);
```

## 配置说明

### 开发环境配置

**catalog-service/application.yml**
```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/catalog_db
    username: catalog_user
    password: catalog_pass
```

**enrollment-service/application.yml**
```yaml
server:
  port: 8082
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/enrollment_db
    username: enrollment_user
    password: enrollment_pass
catalog-service:
  url: http://localhost:8081
```

### Docker 环境配置

通过环境变量覆盖配置，详见 `docker-compose.yml`

## 测试说明

### 功能测试

1. **服务健康检查**
   ```bash
   curl http://localhost:8081/actuator/health
   curl http://localhost:8082/actuator/health
   ```

2. **集成测试**
   ```bash
   ./test-services.sh
   ```

### 测试用例覆盖

- [x] 课程 CRUD 操作
- [x] 学生 CRUD 操作
- [x] 选课业务流程
- [x] 服务间通信
- [x] 错误处理（课程不存在、学生不存在、重复选课等）
- [x] 容量检查
- [x] 并发控制（基础版本）

## 遇到的问题和解决方案



## 开发指南

### 本地开发

1. **启动依赖服务**
   ```bash
   docker-compose up catalog-db enrollment-db -d
   ```

2. **运行应用**
   ```bash
   # 终端1 - 启动课程服务
   cd catalog-service && mvn spring-boot:run

   # 终端2 - 启动选课服务
   cd enrollment-service && mvn spring-boot:run
   ```

### 代码结构

```
course-cloud/
├── catalog-service/          # 课程目录服务
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── enrollment-service/       # 选课服务
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml        # Docker编排
├── test-services.sh          # 测试脚本
├── sql/                      # 数据库脚本
└── README.md
```

