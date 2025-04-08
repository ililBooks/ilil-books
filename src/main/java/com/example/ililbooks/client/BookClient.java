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
import org.springframework.util.ObjectUtils;
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

    public BookApiResponse[] getBooks(String kwd, Integer pageNum, Integer pageSize) {
        URI uri = buildBookApiUri(kwd, pageNum, pageSize);

        // String 타입으로 응답을 받아서 메세지 컨버터가 처리할 수 있도록 하였다.
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException(BOOK_API_RESPONSE_FAILED.getMessage());
        }

        // 응답 내부 데이터 가져오기
        String responseBody = responseEntity.getBody();

        try {

            //json 형태의 데이터 파싱
            BookApiWrapper responseBook = objectMapper.readValue(responseBody, BookApiWrapper.class);
            BookApiResponse[] books = responseBook.getResult();

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
    private URI buildBookApiUri(String kwd, Integer pageNum, Integer pageSize) {
        return UriComponentsBuilder
                .fromUriString("https://www.nl.go.kr/NL/search/openApi/search.do")
                .queryParam("key", apiKey)
                .queryParam("apiType", "json")
                .queryParam("kwd", kwd)
                .queryParam("srchTarget", "total")
                .queryParam("category", "도서")
                .queryParam("pageNum", pageNum)
                .queryParam("pageSize", pageSize)
                .encode()
                .build()
                .toUri();
    }
}
