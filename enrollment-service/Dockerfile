# 多阶段构建
FROM maven:3.9-eclipse-temurin-17 AS builder

# 使用国内 Maven 镜像加速
COPY settings.xml /usr/share/maven/ref/

WORKDIR /app

# 先复制pom.xml，利用Docker缓存层
COPY pom.xml .

# 下载依赖（利用缓存）
RUN mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn -s /usr/share/maven/ref/settings.xml clean package -DskipTests -B

# 第二阶段：使用超小JRE镜像
FROM eclipse-temurin:17-jre-alpine

# 安装最小的健康检查工具
RUN apk add --no-cache wget

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 只复制JAR文件
COPY --from=builder /app/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R spring:spring /app

USER spring

EXPOSE 8080

# 优化JVM参数减少内存占用
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]