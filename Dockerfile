## 1. Java 17을 기반 이미지로 사용
#FROM amazoncorretto:17.0.12
#
## 2. 작업 디렉토리
#WORKDIR /app
#
## 3. 빌드한 JAR 파일을 컨테이너에 복사
#COPY build/libs/ilil-books-0.0.1-SNAPSHOT.jar app.jar
#
## 5. 실행 명령어
#ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.name=application-docker","-Dspring.profiles.active=docker"]


## 1단계: 빌드 환경
#FROM gradle:8.5-jdk17 AS build
#COPY --chown=gradle:gradle . /home/gradle/project
#WORKDIR /home/gradle/project
#RUN gradle clean bootJar --no-daemon
#
## 2단계: 실행 환경
#FROM amazoncorretto:17.0.12
#
## JAR 복사
#COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
#
## 포트 오픈
#EXPOSE 8080
#
## 애플리케이션 실행
#ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.name=application-docker"]

# Build 스테이지
FROM gradle:8.10.2-jdk17 AS builder
# 작업 디렉토리 설정
WORKDIR /apps
# 빌더 이미지에서 애플리케이션 빌드
COPY . /apps

# 실행 스테이지
# OpenJDK 17 slim 기반 이미지 사용
FROM amazoncorretto:17.0.12
# 이미지에 레이블 추가
LABEL type="application"
# 작업 디렉토리 설정
WORKDIR /apps
# 애플리케이션 jar 파일을 컨테이너로 복사
COPY --from=builder /apps/build/libs/ilil-books-0.0.1-SNAPSHOT.jar /apps/app.jar
#COPY wait-for-it.sh /apps/wait-for-it.sh
#RUN chmod +x /apps/wait-for-it.sh

# 애플리케이션이 사용할 포트 노출
EXPOSE 8081
# 애플리케이션을 실행하기 위한 엔트리포인트 정의
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.name=application-docker"]
