package com.example.ililbooks.global.exception;

public class UnauthorizedException extends HandledException {
    public UnauthorizedException() {
        super(ErrorCode.AUTHORIZATION);
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.AUTHORIZATION, message);
    }
}
