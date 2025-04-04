package com.example.ililbooks.domain.book.controller;

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

import java.util.List;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;
import static com.example.ililbooks.domain.user.enums.UserRole.Authority.PUBLISHER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    /**
     * 책 등록 API
     */
    @Secured({ADMIN,PUBLISHER})
    @PostMapping
    public Response<List<BookResponse>> createBook(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize
    ) {
        return Response.created(bookService.createBook(authUser, pageNum, pageSize));
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
    @Secured({ADMIN, PUBLISHER})
    @PatchMapping("/{bookId}")
    public Response<Void> updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BookUpdateRequest bookUpdateRequest
    ) {
        bookService.updateBook(bookId, bookUpdateRequest);
        return Response.empty();
    }

    /**
     * 책 삭제 API
     */
    @Secured({ADMIN, PUBLISHER})
    @DeleteMapping("/{bookId}")
    public Response<Void> deleteBook(
            @PathVariable Long bookId
    ) {
        bookService.deleteBook(bookId);
        return Response.empty();
    }
}
