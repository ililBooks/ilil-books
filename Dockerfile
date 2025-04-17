# TODO 전승민 dockerfile
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
#
### TODO dev dockerfile
## Stage 1: 빌드
#FROM gradle:8.1.1-jdk17 AS builder
#WORKDIR /app
## 테스트 용 경로 설정
## ENV GRADLE_USER_HOME=/app/.gradle
#
#COPY . .
#RUN chmod +x ./gradlew
## TODO: 테스트 용 테스트 코드 제외 설정 -> 테스트 코드 정리 후 변경
#RUN ./gradlew clean build -x test
#
## Stage 2: 이미지 생성
#FROM openjdk:17-jdk-slim
#WORKDIR /app
#COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
#ENTRYPOINT ["java", "-jar", "app.jar"]