
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

### 1. 服务间通信失败

**问题**: enrollment-service 无法连接到 catalog-service

**解决方案**:
- 使用 Docker 自定义网络 `course-network`
- 配置服务发现通过容器名称
- 添加健康检查确保依赖服务就绪

### 2. 数据一致性

**问题**: 选课成功但更新课程人数失败

**解决方案**:
- 采用最终一致性，记录错误日志
- 后续可通过定时任务修复数据不一致
- 生产环境建议使用消息队列

### 3. 端口冲突

**问题**: 多服务端口配置冲突

**解决方案**:
- catalog-service: 8081
- enrollment-service: 8082
- catalog-db: 3307
- enrollment-db: 3308

### 4. 数据库初始化

**问题**: 服务启动时数据库连接失败

**解决方案**:
- 使用 `depends_on` 和健康检查
- 配置服务重试机制
- 添加数据库初始化脚本

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

### 添加新功能

1. 在对应服务中创建 Entity、Repository、Service、Controller
2. 更新 API 文档
3. 添加测试用例
4. 更新 docker-compose.yml（如需要）

## 部署说明

### 生产环境部署

1. **环境变量配置**
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:mysql://生产数据库地址
   export SPRING_DATASOURCE_USERNAME=用户名
   export SPRING_DATASOURCE_PASSWORD=密码
   export CATALOG_SERVICE_URL=http://catalog-service:8081
   ```

2. **构建镜像**
   ```bash
   docker-compose build
   ```

3. **启动服务**
   ```bash
   docker-compose up -d
   ```

### 监控和日志

- 服务健康检查: `/actuator/health`
- 应用日志: Docker 容器日志
- 数据库日志: 数据卷挂载目录

## 版本管理

```bash
# 创建版本标签
git tag -a v1.0.0 -m "Release version 1.0.0 - Microservices architecture"
git push origin v1.0.0
```

## 联系方式

- 项目维护者: [你的姓名]
- 邮箱: [你的邮箱]
- 项目地址: https://github.com/你的用户名/course-cloud

## 许可证

[选择适当的许可证]
```

## 2. 创建 VERSION 文件

```
1.0.0
```

## 3. 创建项目根目录的 pom.xml（多模块管理）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.zjgsu.obl</groupId>
    <artifactId>course-cloud</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <name>Course Cloud - Microservices</name>
    <description>校园选课系统微服务版本</description>
    
    <modules>
        <module>catalog-service</module>
        <module>enrollment-service</module>
    </modules>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-boot.version>3.5.7</spring-boot.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

## 4. 创建 enrollment-service 的 application-prod.yml

```yaml
spring:
  config:
    activate:
      on-profile: "prod"
  datasource:
    url: jdbc:mysql://localhost:3306/enrollment_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: enrollment_user
    password: enrollment_password123
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

# 课程目录服务配置
catalog-service:
  url: http://localhost:8081

server:
  port: 8082
  servlet:
    context-path: /

# 生产环境日志配置
logging:
  level:
    com.zjgsu.obl.enrollment_service: INFO
    org.hibernate.SQL: WARN
    org.springframework.transaction: WARN
    org.springframework.web.client: WARN
  file:
    name: logs/enrollment-service.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 30
```

## 5. 创建 enrollment-service 的 application-docker.yml

```yaml
spring:
  config:
    activate:
      on-profile: "docker"
  application:
    name: enrollment-service
  datasource:
    url: jdbc:mysql://enrollment-db:3306/enrollment_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    username: enrollment_user
    password: enrollment_pass
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 15
      minimum-idle: 3
      connection-timeout: 30000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: false

# 课程目录服务配置 - Docker环境使用服务名
catalog-service:
  url: http://catalog-service:8081

server:
  port: 8082
  servlet:
    context-path: /

# Actuator 健康检查
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
      show-components: always

# Docker 环境日志配置
logging:
  level:
    com.zjgsu.obl.enrollment_service: INFO
    org.springframework.web.client: WARN
    org.hibernate.SQL: WARN
  file:
    name: /app/logs/enrollment-service.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 6. 创建 enrollment-service 的 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.zjgsu.obl</groupId>
        <artifactId>course-cloud</artifactId>
        <version>1.0.0</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    
    <artifactId>enrollment-service</artifactId>
    <version>1.0.0</version>
    <name>enrollment-service</name>
    <description>选课服务 - 负责学生管理和选课业务</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
        
        <finalName>enrollment-service</finalName>
    </build>
    
    <profiles>
        <profile>
            <id>dev</id>
            <properties>
                <spring.profiles.active>dev</spring.profiles.active>
            </properties>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
        </profile>
        <profile>
            <id>prod</id>
            <properties>
                <spring.profiles.active>prod</spring.profiles.active>
            </properties>
        </profile>
        <profile>
            <id>docker</id>
            <properties>
                <spring.profiles.active>docker</spring.profiles.active>
            </properties>
        </profile>
    </profiles>
</project>
```

## 7. 创建 catalog-service 的 application-prod.yml

```yaml
spring:
  config:
    activate:
      on-profile: "prod"
  datasource:
    url: jdbc:mysql://localhost:3306/catalog_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: catalog_user
    password: catalog_password123
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

server:
  port: 8081
  servlet:
    context-path: /

# 生产环境日志配置
logging:
  level:
    com.zjgsu.obl.catalog_service: INFO
    org.hibernate.SQL: WARN
    org.springframework.transaction: WARN
  file:
    name: logs/catalog-service.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 30
```

## 8. 创建 catalog-service 的 application-docker.yml

```yaml
spring:
  config:
    activate:
      on-profile: "docker"
  application:
    name: catalog-service
  datasource:
    url: jdbc:mysql://catalog-db:3306/catalog_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    username: catalog_user
    password: catalog_pass
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 15
      minimum-idle: 3
      connection-timeout: 30000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: false

server:
  port: 8081
  servlet:
    context-path: /

# Actuator 健康检查
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
      show-components: always

# Docker 环境日志配置
logging:
  level:
    com.zjgsu.obl.catalog_service: INFO
    org.hibernate.SQL: WARN
  file:
    name: /app/logs/catalog-service.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 9. 创建 catalog-service 的 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.zjgsu.obl</groupId>
        <artifactId>course-cloud</artifactId>
        <version>1.0.0</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    
    <artifactId>catalog-service</artifactId>
    <version>1.0.0</version>
    <name>catalog-service</name>
    <description>课程目录服务 - 负责课程管理</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
        
        <finalName>catalog-service</finalName>
    </build>
    
    <profiles>
        <profile>
            <id>dev</id>
            <properties>
                <spring.profiles.active>dev</spring.profiles.active>
            </properties>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
        </profile>
        <profile>
            <id>prod</id>
            <properties>
                <spring.profiles.active>prod</spring.profiles.active>
            </properties>
        </profile>
        <profile>
            <id>docker</id>
            <properties>
                <spring.profiles.active>docker</spring.profiles.active>
            </properties>
        </profile>
    </profiles>
</project>
```

## 10. 创建项目结构说明文件

**PROJECT_STRUCTURE.md**
```markdown
# 项目结构说明

## 目录结构

```
course-cloud/
├── catalog-service/              # 课程目录服务
│   ├── src/
│   │   └── main/
│   │       ├── java/com/zjgsu/obl/catalog_service/
│   │       │   ├── model/        # 实体类
│   │       │   ├── repository/   # 数据访问层
│   │       │   ├── service/      # 业务逻辑层
│   │       │   ├── controller/   # 控制层
│   │       │   ├── common/       # 公共组件
│   │       │   ├── exception/    # 异常处理
│   │       │   └── CatalogServiceApplication.java
│   │       └── resources/
│   │           ├── application.yml
│   │           ├── application-prod.yml
│   │           └── application-docker.yml
│   ├── Dockerfile
│   └── pom.xml
├── enrollment-service/           # 选课服务
│   ├── src/
│   │   └── main/
│   │       ├── java/com/zjgsu/obl/enrollment_service/
│   │       │   ├── model/        # 实体类
│   │       │   ├── repository/   # 数据访问层
│   │       │   ├── service/      # 业务逻辑层
│   │       │   ├── controller/   # 控制层
│   │       │   ├── common/       # 公共组件
│   │       │   ├── exception/    # 异常处理
│   │       │   └── EnrollmentServiceApplication.java
│   │       └── resources/
│   │           ├── application.yml
│   │           ├── application-prod.yml
│   │           └── application-docker.yml
│   ├── Dockerfile
│   └── pom.xml
├── sql/                          # 数据库脚本
│   ├── catalog-init.sql
│   └── enrollment-init.sql
├── docker-compose.yml            # Docker编排文件
├── test-services.sh              # 集成测试脚本
├── README.md                     # 项目文档
├── VERSION                       # 版本文件
└── pom.xml                       # 父POM文件
```

## 包命名规范

- 基础包名: `com.zjgsu.obl`
- 服务包名: `catalog_service` / `enrollment_service`
- 分层结构: `model`, `repository`, `service`, `controller`, `common`, `exception`

## 配置文件说明

### 环境配置
- `application.yml` - 默认配置（开发环境）
- `application-prod.yml` - 生产环境配置
- `application-docker.yml` - Docker环境配置

### 配置优先级
Docker环境变量 > application-docker.yml > application-prod.yml > application.yml
```

现在整个微服务项目已经完整了！包含：

✅ **核心服务**: catalog-service, enrollment-service  
✅ **容器化配置**: Dockerfile, docker-compose.yml  
✅ **环境配置**: 开发、生产、Docker环境  
✅ **数据库**: MySQL配置和初始化脚本  
✅ **文档**: 完整的README和项目说明  
✅ **测试**: 集成测试脚本  
✅ **构建**: Maven多模块配置

你可以按照 README.md 中的说明来构建、部署和测试整个系统了！