package com.example.ililbooks.global.redis;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonClient {

    @Bean
    public org.redisson.api.RedissonClient redissonClient() { // RedissonClient 반환시 충돌로 패키지 포함 명시
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379")
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10);
        return Redisson.create(config);
    }
}
