# Stage 1: 빌드
FROM gradle:8.1.1-jdk17 AS builder
WORKDIR /app
# 테스트 용 경로 설정
ENV GRADLE_USER_HOME=/app/.gradle

COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build --no-daemon

# Stage 2: 이미지 생성
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]