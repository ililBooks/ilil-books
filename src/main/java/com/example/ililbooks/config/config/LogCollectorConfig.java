package com.example.ililbooks.config.config;

import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.log.collector.DataBaseLogCollector;
import com.example.ililbooks.global.log.collector.LogCollector;
import com.example.ililbooks.global.log.repository.SystemLogRepository;
import com.example.ililbooks.global.log.service.SystemLogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogCollectorConfig {

    @Bean
    public LogCollector logCollector(SystemLogRepository systemLogRepository, UserRepository userRepository) {
        return new DataBaseLogCollector(systemLogRepository, userRepository);
    }
}