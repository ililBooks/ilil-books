package com.example.ililbooks.client;

import com.example.ililbooks.client.dto.BookApiResponse;
import com.example.ililbooks.client.dto.BookApiWrapper;
import com.example.ililbooks.global.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Component
public class BookClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    //발급 키
    @Value("${book.api.key}")
    private String apiKey;

    public BookClient(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        this.restTemplate = builder.build();
        this.objectMapper = objectMapper;
    }

    public BookApiResponse[] getBooks(String nickname, Integer pageNum, Integer pageSize) {
        URI uri = buildBookApiUri(nickname, pageNum, pageSize);

        // 응답을 문자열로 받기
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException(BOOK_API_RESPONSE_FAILED.getMessage());
        }

        String responseBody = responseEntity.getBody();

        try {

            BookApiWrapper wrapper = objectMapper.readValue(responseBody, BookApiWrapper.class);

            BookApiResponse[] books = wrapper.getResult();

            if (books == null || books.length == 0) {
                throw new NotFoundException(NOT_FOUND_BOOK.getMessage());
            }

            return books;

        } catch (Exception e) {
            throw new RuntimeException(BOOK_PARSING_FAILED.getMessage(), e);
        }
    }

    /**
     * kwd: 검색어 (닉네임(출판사)으로 검색)
     * srchTarget: 검색 조건은 발행자(출판사)로 설정
     * category: 도서에 관련된 것들만 검색
     * pageNum: 현재 페이지
     * pageSize: 페이지 크기 (default 10건)
     */
    private URI buildBookApiUri(String nickname, Integer pageNum, Integer pageSize) {
        return UriComponentsBuilder
                .fromUriString("https://www.nl.go.kr/NL/search/openApi/search.do")
                .queryParam("key", apiKey)
                .queryParam("apiType", "json")
                .queryParam("kwd", nickname)
                .queryParam("srchTarget", "publisher")
                .queryParam("category", "도서")
                .queryParam("pageNum", pageNum)
                .queryParam("pageSize", pageSize)
                .encode()
                .build()
                .toUri();
    }
}
