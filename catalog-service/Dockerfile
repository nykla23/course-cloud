# 多阶段构建：构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 文件（利用 Docker 缓存层）
COPY pom.xml .

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用（跳过测试）
RUN mvn clean package -DskipTests -B

# 多阶段构建：运行阶段
FROM eclipse-temurin:17-jre-alpine

# 应用元数据
LABEL maintainer="your-email@example.com"
LABEL version="1.0.0"
LABEL description="Catalog Service for Campus Course Selection System"

# 安装必要的工具
RUN apk add --no-cache curl

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R spring:spring /app

# 切换到非 root 用户
USER spring

# 暴露端口
EXPOSE 8081

# 环境变量配置
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

# 健康检查配置
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# 优化 JVM 启动参数
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]