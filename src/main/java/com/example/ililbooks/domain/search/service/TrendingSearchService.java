package com.example.ililbooks.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrendingSearchService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TRENDING_SEARCH_KEY = "trending-search";
}
