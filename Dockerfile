# Build 스테이지
FROM gradle:8.10.2-jdk17 AS builder
# 작업 디렉토리 설정
WORKDIR /apps
# 빌더 이미지에서 애플리케이션 빌드
COPY . /apps
# 어플리케이션 빌드
RUN chmod +x gradlew
RUN ./gradlew build

# 실행 스테이지
# OpenJDK 17 slim 기반 이미지 사용
FROM amazoncorretto:17.0.12
# 이미지에 레이블 추가
LABEL type="application"
# 작업 디렉토리 설정
WORKDIR /apps
# 애플리케이션 jar 파일을 컨테이너로 복사
COPY --from=builder /apps/build/libs/ilil-books-0.0.1-SNAPSHOT.jar /apps/app.jar
# 애플리케이션이 사용할 포트 노출
EXPOSE 8081
# 애플리케이션을 실행하기 위한 엔트리포인트 정의
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.name=application-docker"]