FROM openjdk:21
WORKDIR /app
ADD target/eightandhalf-chat-0.0.1-SNAPSHOT.jar .
ENV SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true
ENV TZ=Asia/Shanghai
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/eightandhalf-chat-0.0.1-SNAPSHOT.jar"]