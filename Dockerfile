# 使用官方的OpenJDK镜像作为基础镜像
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 将构建好的JAR文件复制到容器中
COPY target/fire-monitor.jar /app/fire-monitor.jar

# 配置容器启动时的命令
ENTRYPOINT ["java", "-jar", "fire-monitor.jar"]

# 暴露应用端口（根据你的Spring Boot应用端口修改）
EXPOSE 9500