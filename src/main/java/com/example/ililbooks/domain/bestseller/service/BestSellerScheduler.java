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
        saveBestSellers(PeriodType.DAILY, LocalDate.now());
    }

    /* 매월 1일에 monthly 베스트셀러 저장 */
    @Scheduled(cron = "0 0 0 1 * *")
    public void saveMonthlyBestSeller() {
        saveBestSellers(PeriodType.MONTHLY, LocalDate.now());
    }

    /* 매년 1월 1일에 yearly 베스트셀러 저장 */
    @Scheduled(cron = "0 0 0 1 1 *")
    public void saveYearlyBestSeller() {
        saveBestSellers(PeriodType.YEARLY, LocalDate.now());
    }

    /* 베스트셀러 저장 */
    private void saveBestSellers(PeriodType type, LocalDate date) {
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
                    .periodType(type)
                    .date(instant)
                    .ranking(ranking++)
                    .build();
            bestSellers.add(bestSeller);
        }
        bestSellerRepository.saveAll(bestSellers);
    }

    private String generateKey(PeriodType type, LocalDate date) {
        return switch (type) {
            case DAILY -> "bestseller:daily:" + date;
            case MONTHLY -> "bestseller:monthly:" + YearMonth.from(date);
            case YEARLY -> "bestseller:yearly:" + Year.from(date);
        };
    }

    private Instant convertToInstant(PeriodType type, LocalDate date) {
        return switch (type) {
            case DAILY -> date.atStartOfDay().toInstant(ZoneOffset.UTC);
            case MONTHLY -> YearMonth.from(date).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            case YEARLY -> Year.from(date).atMonth(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        };
    }
}
