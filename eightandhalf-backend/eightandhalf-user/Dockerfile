FROM openjdk:21
WORKDIR /app
ADD target/eightandhalf-user-0.0.1-SNAPSHOT.jar .
ENV SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true
ENV TZ=Asia/Shanghai
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/eightandhalf-user-0.0.1-SNAPSHOT.jar"]