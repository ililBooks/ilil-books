package com.example.ililbooks.domain.user.controller;

import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.user.dto.request.UserDeleteRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdatePasswordRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdateRequest;
import com.example.ililbooks.domain.user.dto.response.UserResponse;
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

    /* 회원 조회 (본인) */
    @GetMapping
    public Response<UserResponse> findUser(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return Response.of(userService.findUser(authUser));
    }

    /* 회원 수정 */
    @PatchMapping
    public Response<AuthAccessTokenResponse> updateUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        AuthAccessTokenResponse authAccessTokenResponse = userService.updateUser(authUser, userUpdateRequest);
        return Response.of(authAccessTokenResponse);
    }

    /* 회원 비밀번호 수정 */
    @PatchMapping("/password")
    public Response<Void> updatePasswordUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest
    ) {
        userService.updatePasswordUser(authUser, userUpdatePasswordRequest);
        return Response.empty();
    }

    /* 회원 탈퇴 */
    @DeleteMapping
    public Response<Void> deleteUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserDeleteRequest userDeleteRequest
    ) {
        userService.deleteUser(authUser, userDeleteRequest);
        return Response.empty();
    }
}
