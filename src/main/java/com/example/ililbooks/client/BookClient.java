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

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_BOOK;

@Component
public class BookClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${book.api.key}")
    private String apiKey;

    public BookClient(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        this.restTemplate = builder.build();
        this.objectMapper = objectMapper;
    }

    public BookApiResponse[] getBooks(Integer pageNum, Integer pageSize) {
        URI uri = buildBookApiUri(pageNum, pageSize);

        // 응답을 문자열로 받기
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException("도서 API 응답 실패");
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
            throw new RuntimeException("도서 정보 파싱 실패", e);
        }
    }

    private URI buildBookApiUri(Integer pageNum, Integer pageSize) {
        return UriComponentsBuilder
                .fromUriString("https://www.nl.go.kr/NL/search/openApi/search.do")
                .queryParam("key", apiKey)
                .queryParam("apiType", "json")
                .queryParam("kwd", "길벗") // 검색어 (필수로 넣어주야 한다.)
                .queryParam("srchTarget", "publisher") // 검색 조건 (전체 - 책, 제목, 저자, 발행자(출판사), 청구 기호)으로 설정
                .queryParam("category", "도서")
                .queryParam("pageNum", pageNum)
                .queryParam("pageSize", pageSize)
                .encode()
                .build()
                .toUri();
    }
}
