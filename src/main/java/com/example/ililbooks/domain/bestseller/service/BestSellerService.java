package com.example.ililbooks.domain.bestseller.service;

import com.example.ililbooks.domain.bestseller.dto.response.BestSellerChartResponse;
import com.example.ililbooks.domain.bestseller.entity.BestSeller;
import com.example.ililbooks.domain.bestseller.enums.PeriodType;
import com.example.ililbooks.domain.bestseller.repository.BestSellerRepository;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.entity.BookImage;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

import static com.example.ililbooks.domain.bestseller.enums.PeriodType.*;
import static com.example.ililbooks.global.exception.ErrorMessage.BOOK_ID_NOT_FOUND_IN_REDIS;

@Service
@RequiredArgsConstructor
public class BestSellerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final BookService bookService;
    private final BestSellerRepository bestSellerRepository;

    private static final Duration TTL_FOR_DAILY_KEY = Duration.ofDays(3);
    private static final Duration TTL_FOR_MONTHLY_KEY = Duration.ofDays(40);
    private static final Duration TTL_FOR_YEARLY_KEY = Duration.ofDays(400);


    @Transactional(readOnly = true)
    public List<BestSellerChartResponse> getBestSellerChart(PeriodType type, String date) {
        LocalDate localDate = LocalDate.parse(date);

        Instant instant = convertToInstant(type, localDate);

        List<BestSeller> bestSellersFromDB = bestSellerRepository.findAllByPeriodTypeAndDate(type, instant);

        if (!bestSellersFromDB.isEmpty()) {
            return bestSellersFromDB.stream()
                    .map(bestSeller ->
                    {
                        Book book = bookService.findBookByIdOrElseThrow(bestSeller.getBookId());
                        Optional<BookImage> image = bookService.findFirstByBookId(book.getId());
                        return BestSellerChartResponse.builder()
                                .title(book.getTitle())
                                .author(book.getAuthor())
                                .category(book.getCategory())
                                .publisher(book.getPublisher())
                                .rank(bestSeller.getRanking())
                                .saleStatus(book.getSaleStatus().name())
                                .limitedType(book.getLimitedType().name())
                                .price(book.getPrice())
                                .imageUrl(image.map(BookImage::getImageUrl).orElse(null))
                                .build();
                    })
                    .toList();
        }

        String key = generateKey(type, localDate);

        Set<String> bookIds = redisTemplate.opsForZSet().reverseRange(key, 0, 9);

        if (bookIds.isEmpty()) {
            throw new NotFoundException(BOOK_ID_NOT_FOUND_IN_REDIS.getMessage());
        }

        List<BestSellerChartResponse> result = new ArrayList<>();

        int rank = 1;

        for (String bookId : bookIds) {
            Book book = bookService.findBookByIdOrElseThrow(Long.parseLong(bookId));
            Optional<BookImage> image = bookService.findFirstByBookId(book.getId());

            BestSellerChartResponse response = BestSellerChartResponse.builder()
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .category(book.getCategory())
                    .publisher(book.getPublisher())
                    .rank(rank++)
                    .saleStatus(book.getSaleStatus().name())
                    .limitedType(book.getLimitedType().name())
                    .price(book.getPrice())
                    .imageUrl(image.map(BookImage::getImageUrl).orElse(null))
                    .build();
            result.add(response);
        }
        return result;
    }

    private void increaseBookSales(Long bookId, int quantity) {
        LocalDate now = LocalDate.now();
        String dailyKey = generateKey(DAILY, now);
        String monthlyKey = generateKey(MONTHLY, now);
        String yearlyKey = generateKey(YEARLY, now);

        for (int i = 0; i < quantity; i++) {
            redisTemplate.opsForZSet().incrementScore(dailyKey, bookId.toString(), 1);
            redisTemplate.opsForZSet().incrementScore(monthlyKey, bookId.toString(), 1);
            redisTemplate.opsForZSet().incrementScore(yearlyKey, bookId.toString(), 1);
        }
        redisTemplate.expire(dailyKey, TTL_FOR_DAILY_KEY);
        redisTemplate.expire(monthlyKey, TTL_FOR_MONTHLY_KEY);
        redisTemplate.expire(yearlyKey, TTL_FOR_YEARLY_KEY);
    }

    /* Quantity 만큼 판매량 증가 */
    public void increaseBookSalesByQuantity(Map<Long, CartItem> bookMap) {
        for (CartItem cartItem : bookMap.values()) {
            int quantity = cartItem.getQuantity();
            increaseBookSales(cartItem.getBookId(), quantity);
        }
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
