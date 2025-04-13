package com.example.ililbooks.domain.book.controller;

import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.dto.response.BookWithImagesResponse;
import com.example.ililbooks.domain.book.dto.response.BookListResponse;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import com.example.ililbooks.global.image.dto.request.ImageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
@Tag(name = "Book", description = "책 관련 API")
public class BookController {

    private final BookService bookService;

    /**
     * 직접 입력하여 책을 단건 저장하는 API
     */
    @Operation(summary = "책 직접 등록", description = "관리자가 책 정보를 직접 입력하여 저장하는 API입니다.")
    @Secured(ADMIN)
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
    @Operation(summary = "외부 Open API로 책 등록", description = "외부 Open API에서 책 정보를 검색하여 저장하는 API입니다.")
    @Secured(ADMIN)
    @PostMapping("/open-api")
    public Response<Void> createBooksByOpenApi(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam String keyword
    ) {
        bookService.createBookByOpenApi(authUser, pageNum, pageSize);
        return Response.empty();
    }

    /**
     * 책 이미지 업로드 API
     */
    @Operation(summary = "책 이미지 업로드", description = "S3에 올라간 책에 대한 이미지를 DB에 업로드하는 API입니다.")
    @Secured(ADMIN)
    @PostMapping("/{bookId}/image")
    public Response<Void> uploadBookImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long bookId,
            @ModelAttribute ImageRequest imageRequest
    ) {
        bookService.uploadBookImage(authUser, bookId, imageRequest);
        return Response.empty();
    }

    /**
     * 책 단건 조회 API
     */
    @Operation(summary = "책 상세 조회", description = "책 ID를 기준으로 상세 정보를 조회하는 API입니다.")
    @GetMapping("/{bookId}")
    public Response<BookWithImagesResponse> findBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(bookService.findBookResponse(bookId, pageNum, pageSize));
    }

    /**
     * 책 다건 조회 API
     */
    @Operation(summary = "책 목록 조회", description = "등록된 모든 책을 페이징 처리하여 조회하는 API입니다.")
    @GetMapping("/all")
    public Response<Page<BookListResponse>> getBooks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(bookService.getBooks(pageNum, pageSize));
    }

    /**
     * 책 수정 API
     */
    @Operation(summary = "책 정보 수정", description = "책 ID를 기준으로 책의 정보를 수정하는 API입니다.")
    @Secured(ADMIN)
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
    @Operation(summary = "책 삭제", description = "책 ID를 기준으로 책을 삭제하는 API입니다.")
    @Secured(ADMIN)
    @DeleteMapping("/{bookId}")
    public Response<Void> deleteBook(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long bookId
    ) {
        bookService.deleteBook(authUser, bookId);
        return Response.empty();
    }
}
