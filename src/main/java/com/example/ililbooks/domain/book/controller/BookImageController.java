package com.example.ililbooks.domain.book.controller;

import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/book-images")
public class BookImageController {
    private final BookService bookService;

    /**
     * 책 이미지 삭제 API
     */
    @Secured(ADMIN)
    @DeleteMapping("/{imageId}")
    public Response<Void> deleteBookImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long imageId
    ) {
        bookService.deleteBookImage(authUser, imageId);
        return Response.empty();
    }
}
