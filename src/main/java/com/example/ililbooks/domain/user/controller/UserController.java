package com.example.ililbooks.domain.user.controller;

import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.user.dto.request.UserDeleteRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdateAlertRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdatePasswordRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdateRequest;
import com.example.ililbooks.domain.user.dto.response.UserResponse;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;

    /* 회원 조회 (본인) */
    @Operation(summary = "회원 정보 조회", description = "본인의 회원 정보를 조회할 수 있습니다.")
    @GetMapping
    public Response<UserResponse> findUser(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return Response.of(userService.findUser(authUser));
    }

    /* 회원 수정 */
    @Operation(summary = "회원 정보 수정", description = "닉네임, 주소, 연락처 등 본인의 회원 정보를 수정할 수 있습니다.")
    @PatchMapping
    public Response<AuthAccessTokenResponse> updateUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        AuthAccessTokenResponse authAccessTokenResponse = userService.updateUser(authUser, userUpdateRequest);
        return Response.of(authAccessTokenResponse);
    }

    /* 회원 비밀번호 수정 */
    @Operation(summary = "회원 비밀번호 수정", description = "기존 비밀번호 및 새 비밀번호를 입력해 회원 비밀번호를 수정할 수 있습니다.")
    @PatchMapping("/password")
    public Response<Void> updatePasswordUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest
    ) {
        userService.updatePasswordUser(authUser, userUpdatePasswordRequest);
        return Response.empty();
    }

    /* 회원 탈퇴 */
    @Operation(summary = "회원 탈퇴", description = "가입된 유저가 서비스를 탈퇴 할 수 있습니다.")
    @DeleteMapping
    public Response<Void> deleteUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserDeleteRequest userDeleteRequest
    ) {
        userService.deleteUser(authUser, userDeleteRequest);
        return Response.empty();
    }

    /* 알림 수신 동의/거부 */
    @Secured(USER)
    @Operation(summary = "알림 수신 동의 및 거부", description = "예약 및 주문 알림 수신 동의/거부를 할 수 있습니다.")
    @PatchMapping("notifications")
    public Response<Void> updateAlert(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "false") boolean receive
    ) {
        userService.updateAlert(authUser, receive);
        return Response.empty();
    }
}
