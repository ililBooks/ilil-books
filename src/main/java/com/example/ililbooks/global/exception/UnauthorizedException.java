package com.example.ililbooks.global.exception;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException() {
        super(ErrorCode.AUTHORIZATION);
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.AUTHORIZATION, message);
    }
}
