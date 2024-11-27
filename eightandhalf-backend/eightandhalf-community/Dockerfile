FROM openjdk:21
WORKDIR /app
ADD target/eightandhalf-community-0.0.1-SNAPSHOT.jar .
ENV SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true
ENV TZ=Asia/Shanghai
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/eightandhalf-community-0.0.1-SNAPSHOT.jar"]