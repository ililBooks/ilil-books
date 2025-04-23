package com.example.ililbooks.config.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    private final RedisProperties redisProperties;

    public RedissonConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        config.useSingleServer()
                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setTimeout(3000);

        String password = redisProperties.getPassword();
        if (password != null && !password.isBlank()) {
            config.useSingleServer().setPassword(password);
        }

        return Redisson.create(config);
    }
}
