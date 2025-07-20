FROM gradle:7.6-jdk17 AS builder
WORKDIR /app

COPY . .

RUN gradle bootJar --no-daemon

FROM openjdk:17-jdk-slim
VOLUME /tmp

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
