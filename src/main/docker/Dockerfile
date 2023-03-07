FROM ascdc/jdk8
VOLUME /tmp
ADD zjfh-api-0.0.1-SNAPSHOT.jar /api.jar
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT ["java","-jar","/api.jar"]
