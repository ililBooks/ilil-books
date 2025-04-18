package com.example.ililbooks.domain.search.controller;

import com.example.ililbooks.domain.search.dto.BookSearchResponse;
import com.example.ililbooks.domain.search.service.BookSearchService;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "DB 검색 API")
public class SearchControllerV1 {

    private final BookSearchService bookSearchService;

    @Operation(summary = "DB 책 조회", description = "SQL 로 책 조회")
    @GetMapping
    public Response<Page<BookSearchResponse>> searchBooksInDB(
            @RequestParam String q,
            @PageableDefault(size = 500, page = 1) Pageable pageable
    ) {
        return Response.of(bookSearchService.searchBooksV1(q, pageable));
    }
}
