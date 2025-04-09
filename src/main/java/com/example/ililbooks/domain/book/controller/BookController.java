package com.example.ililbooks.domain.book.controller;

import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.dto.response.BookWithImagesResponse;
import com.example.ililbooks.domain.book.dto.response.BookListResponse;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;
import static com.example.ililbooks.domain.user.enums.UserRole.Authority.PUBLISHER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    /**
     * 직접 입력하여 책을 단건 저장하는 API
     */
    @Secured({ADMIN})
    @PostMapping
    public Response<BookResponse> creatBook(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody BookCreateRequest bookCreateRequest
    ) {
        return Response.created(bookService.createBook(authUser, bookCreateRequest));
    }

    /**
     * 외부 Open API를 통해 책 정보를 가져와 저장하는 API
     */
    @Secured({ADMIN})
    @PostMapping("/open-api")
    public Response<Void> createBooksByOpenApi(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam String keyword
    ) {
        bookService.createBookByOpenApi(authUser, pageNum, pageSize, keyword);
        return Response.empty();
    }

    /**
     * 책 이미지 업로드 API
     */
    @Secured({ADMIN})
    @PostMapping("/{bookId}/image")
    public Response<Void> uploadBookImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long bookId,
            @RequestParam String imageUrl
    ) {
        bookService.uploadBookImage(authUser, bookId, imageUrl);
        return Response.empty();
    }

    /**
     * 책 단건 조회 API
     */
    @GetMapping("/{bookId}")
    public Response<BookWithImagesResponse> getBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(bookService.getBookResponse(bookId, pageNum, pageSize));
    }

    /**
     * 책 다건 조회 API
     */
    @GetMapping
    public Response<Page<BookListResponse>> getBooks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(bookService.getBooks(pageNum, pageSize));
    }

    /**
     * 책 수정 API
     */
    @Secured({ADMIN})
    @PatchMapping("/{bookId}")
    public Response<Void> updateBook(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long bookId,
            @Valid @RequestBody BookUpdateRequest bookUpdateRequest
    ) {
        bookService.updateBook(authUser, bookId, bookUpdateRequest);
        return Response.empty();
    }

    /**
     * 책 삭제 API
     */
    @Secured({ADMIN})
    @DeleteMapping("/{bookId}")
    public Response<Void> deleteBook(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long bookId
    ) {
        bookService.deleteBook(authUser, bookId);
        return Response.empty();
    }
}
