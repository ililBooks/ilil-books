package com.example.ililbooks.config.config;

import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.log.collector.CloudWatchLogCollector;
import com.example.ililbooks.global.log.collector.LogCollector;
import com.example.ililbooks.global.log.collector.Slf4jLogCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogCollectorConfig {

    @Bean
    public LogCollector logCollector() {
        return new Slf4jLogCollector(); // 개발 단계에서는 Slf4 로그 컬렉터 사용 (콘솔에만 로그 출력) - 운영 단계에서는 CloudWatchLogCollector 로 변경 AWS 로 로그 전송
    }
}