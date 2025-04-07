# 1. Java 17을 기반 이미지로 사용
FROM amazoncorretto:17.0.12

# 2. 작업 디렉토리
WORKDIR /app

# 3. 빌드한 JAR 파일을 컨테이너에 복사
COPY ilil-books-0.0.1-SNAPSHOT.jar app.jar

# 4. 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]


# 1단계: 빌드 환경
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle clean bootJar --no-daemon

# 2단계: 실행 환경
FROM amazoncorretto:17.0.12

# JAR 복사
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# 포트 오픈
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
