FROM java:8
MAINTAINER kexiaomeng
EXPOSE 8080

VOLUME /tmp
ADD /target/renren-fast.jar  /app.jar
# touch表示有的话就不做动作，没有就覆盖
RUN bash -c 'touch /app.jar'
# 执行命令
ENTRYPOINT ["java","-jar","/app.jar"]
