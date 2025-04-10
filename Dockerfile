# Stage 1: 빌드
FROM gradle:8.1.1-jdk17 AS builder
WORKDIR /app
# 테스트 용 경로 설정
# ENV GRADLE_USER_HOME=/app/.gradle

COPY . .
RUN chmod +x ./gradlew
# TODO: 테스트 용 테스트 코드 제외 설정 -> 테스트 코드 정리 후 변경
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: 이미지 생성
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]