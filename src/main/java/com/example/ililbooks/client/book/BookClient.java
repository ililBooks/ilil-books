package com.example.ililbooks.client.book;

import com.example.ililbooks.client.book.dto.BookApiResponse;
import com.example.ililbooks.client.book.dto.BookApiWrapper;
import com.example.ililbooks.global.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Component
public class BookClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    //발급 키
    @Value("${book.api.key}")
    private String apiKey;

    public BookClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public BookApiResponse[] findBooks(String keyword, Pageable pageable) {
        URI uri = buildBookApiUri(keyword, pageable);

        String responseBody = webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        res -> Mono.error(new RuntimeException(BOOK_API_RESPONSE_FAILED.getMessage())))
                .bodyToMono(String.class)
                .block();

        try {

            //json 형태의 데이터 파싱
            BookApiWrapper responseBook = objectMapper.readValue(responseBody, BookApiWrapper.class);
            BookApiResponse[] books = responseBook.result();

            //검색된 책이 없는 경우
            if (ObjectUtils.isEmpty(books)) {
                throw new NotFoundException(NOT_FOUND_BOOK.getMessage());
            }

            return books;

        } catch (Exception e) {
            throw new RuntimeException(BOOK_PARSING_FAILED.getMessage(), e);
        }
    }

    /**
     * kwd: 검색어
     * srchTarget: 검색 조건 (title, author, publisher, cheonggu)
     * category: '도서'에 관련된 것들만 검색
     * pageNum: 현재 페이지
     * pageSize: 페이지 크기 (default 10건)
     */
    private URI buildBookApiUri(String keyword, Pageable pageable) {
        return UriComponentsBuilder
                .fromUriString("https://www.nl.go.kr/NL/search/openApi/search.do")
                .queryParam("key", apiKey)
                .queryParam("apiType", "json")
                .queryParam("kwd", keyword)
                .queryParam("srchTarget", "total")
                .queryParam("category", "도서")
                .queryParam("pageNum", pageable.getPageNumber())
                .queryParam("pageSize", pageable.getPageSize())
                .encode()
                .build()
                .toUri();
    }
}
