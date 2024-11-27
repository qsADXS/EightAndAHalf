FROM openjdk:21
WORKDIR /app
ADD target/eightandhalf-gateway-0.0.1-SNAPSHOT.jar .
ENV SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true
ENV TZ=Asia/Shanghai
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/eightandhalf-gateway-0.0.1-SNAPSHOT.jar"]