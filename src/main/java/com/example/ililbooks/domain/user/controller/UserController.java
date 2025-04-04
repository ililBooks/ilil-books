package com.example.ililbooks.domain.user.controller;

import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.user.dto.request.UserUpdateRequest;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    /* 회원 수정 */
    @PatchMapping
    public Response<AuthAccessTokenResponse> updateUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        AuthAccessTokenResponse authAccessTokenResponse = userService.updateUser(authUser, userUpdateRequest);
        return Response.of(authAccessTokenResponse);
    }
}
