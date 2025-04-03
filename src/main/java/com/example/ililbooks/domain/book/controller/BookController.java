package com.example.ililbooks.domain.book.controller;

import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.PUBLISHER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    /**
     * 책 등록 API
     */
    @Secured(PUBLISHER)
    @PostMapping
    public Response<BookResponse> createBook(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody BookCreateRequest bookCreateRequest
    ) {
        return Response.of(bookService.createBook(authUser, bookCreateRequest));
    }

    /**
     * 책 단건 조회 API
     */
    @GetMapping("/{bookId}")
    public Response<BookResponse> getBook(
            @PathVariable Long bookId
    ) {
        return Response.of(bookService.getBookResponse(bookId));
    }

    /**
     * 책 수정 API
     */
    @Secured(PUBLISHER)
    @PatchMapping("/{bookId}")
    public void updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BookUpdateRequest bookUpdateRequest
    ) {
        bookService.updateBook(bookId, bookUpdateRequest);
    }

    /**
     * 책 삭제 API
     */
    @Secured(PUBLISHER)
    @DeleteMapping("/{bookId}")
    public void deleteBook(
            @PathVariable Long bookId
    ) {
        bookService.deleteBook(bookId);
    }
}
