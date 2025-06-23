# 指明jdk, 如果没有指明操作系统, 默认ubuntu或者debian
FROM openjdk:24-jdk

# 工作目录
WORKDIR /DockerApp

# 复制target下的jar包到工作目录
COPY target/manage-0.0.1-SNAPSHOT.jar .

#启动容器时执行的命令
CMD ["java", "-jar", "manage-0.0.1-SNAPSHOT.jar"]