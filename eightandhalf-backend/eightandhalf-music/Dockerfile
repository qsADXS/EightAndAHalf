FROM openjdk:21
WORKDIR /app
ADD target/eightandhalf-music-0.0.1-SNAPSHOT.jar .
ENV SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java","-jar","/app/eightandhalf-music-0.0.1-SNAPSHOT.jar"]