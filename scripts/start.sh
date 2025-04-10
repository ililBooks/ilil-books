#!/bin/bash

APP_NAME=my-app
JAR_NAME=$(ls /home/ec2-user/app/*.jar | head -n 1)
LOG_FILE=/home/ec2-user/app/logs/$APP_NAME.log
PID_FILE=/home/ec2-user/app/$APP_NAME.pid

echo "> Starting $JAR_NAME"

# 백그라운드로 Spring Boot 앱 실행
nohup java -jar "$JAR_NAME" > "$LOG_FILE" 2>&1 &

# PID 저장
echo $! > "$PID_FILE"

echo "> Application started with PID $(cat $PID_FILE)"