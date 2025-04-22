package com.example.ililbooks.domain.bestseller.service;

import com.example.ililbooks.domain.bestseller.entity.BestSeller;
import com.example.ililbooks.domain.bestseller.enums.PeriodType;
import com.example.ililbooks.domain.bestseller.repository.BestSellerRepository;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.ililbooks.global.exception.ErrorMessage.BOOK_ID_NOT_FOUND_IN_REDIS;
import static com.example.ililbooks.global.exception.ErrorMessage.PERIOD_TYPE_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class BestSellerScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final BestSellerRepository bestSellerRepository;

    /* 매일 자정에 daily 베스트셀러 저장 */
    @Scheduled(cron = "0 0 0 * * *")
    public void saveDailyBestSeller() {
        saveBestSellers("daily", LocalDate.now());
    }

    /* 매월 1일에 monthly 베스트셀러 저장 */
    @Scheduled(cron = "0 0 0 1 * *")
    public void saveMonthlyBestSeller() {
        saveBestSellers("monthly", LocalDate.now());
    }

    /* 매년 1월 1일에 yearly 베스트셀러 저장 */
    @Scheduled(cron = "0 0 0 1 1 *")
    public void saveYearlyBestSeller() {
        saveBestSellers("yearly", LocalDate.now());
    }

    /* 베스트셀러 저장 */
    private void saveBestSellers(String type, LocalDate date) {
        String key = generateKey(type, date);

        int ranking = 1;
        List<BestSeller> bestSellers = new ArrayList<>();

        Instant instant = convertToInstant(type, date);

        Set<String> bookIds = redisTemplate.opsForZSet().reverseRange(key, 0, 9);

        if (bookIds.isEmpty()) {
            throw new NotFoundException(BOOK_ID_NOT_FOUND_IN_REDIS.getMessage());
        }

        for (String bookId : bookIds) {
            BestSeller bestSeller = BestSeller.builder()
                    .bookId(Long.parseLong(bookId))
                    .periodType(PeriodType.valueOf(type.toUpperCase()))
                    .date(instant)
                    .ranking(ranking++)
                    .build();
            bestSellers.add(bestSeller);
        }
        bestSellerRepository.saveAll(bestSellers);
    }

    private String generateKey(String type, LocalDate date) {
        return switch (type) {
            case "daily" -> "bestseller:daily:" + date;
            case "monthly" -> "bestseller:monthly:" + YearMonth.from(date);
            case "yearly" -> "bestseller:yearly:" + Year.from(date);
            default -> throw new IllegalArgumentException(PERIOD_TYPE_NOT_FOUND.getMessage());
        };
    }

    private Instant convertToInstant(String type, LocalDate date) {
        return switch (type) {
            case "daily" -> date.atStartOfDay().toInstant(ZoneOffset.UTC);
            case "monthly" -> YearMonth.from(date).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            case "yearly" -> Year.from(date).atMonth(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            default -> throw new IllegalArgumentException(PERIOD_TYPE_NOT_FOUND.getMessage());
        };
    }
}
