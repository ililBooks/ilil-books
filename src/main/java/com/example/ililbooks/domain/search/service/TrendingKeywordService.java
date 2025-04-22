package com.example.ililbooks.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrendingKeywordService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TRENDING_KEYWORD_KEY = "trending:keyword:";

    public void increaseTrendingCount(String keyword) {
        String key = generateKey(LocalDateTime.now());
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1);
        redisTemplate.expire(key, Duration.ofHours(3));
    }

    public List<String> getTrendingChart() {
        String currentHourKey = generateKey(LocalDateTime.now());
        String lastHourKey = generateKey(LocalDateTime.now().minusHours(1));
        String tempKey = "trending:temp" + UUID.randomUUID();

        Set<String> currentHourKeywords = redisTemplate.opsForZSet()
                .reverseRange(currentHourKey, 0, 9);

        // 현재 시간의 인기 검색어 키워드가 10개 이상이면 현재 시간의 키워드 반환
        if (currentHourKeywords.size() >= 10) {
            return new ArrayList<>(currentHourKeywords);
        }

        // 현재 시간의 인기 검색어 키워드가 10개 이하일 때 이전 시간의 키워드와 병합해서 임시키에 저장
        redisTemplate.opsForZSet().unionAndStore(currentHourKey, lastHourKey, tempKey);

        Set<String> mergedKeywords = redisTemplate.opsForZSet().reverseRange(tempKey, 0, 9);

        redisTemplate.delete(tempKey);

        return mergedKeywords != null ? new ArrayList<>(mergedKeywords) : List.of();
    }

    private String generateKey(LocalDateTime time) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
        return TRENDING_KEYWORD_KEY + time.format(dateTimeFormatter);
    }
}
