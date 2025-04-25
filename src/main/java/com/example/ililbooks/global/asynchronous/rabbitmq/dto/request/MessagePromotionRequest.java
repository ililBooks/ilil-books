package com.example.ililbooks.global.asynchronous.rabbitmq.dto.request;

public record MessagePromotionRequest (
        String email,
        String nickname
){
    public static MessagePromotionRequest of(String email, String nickname) {
        return new MessagePromotionRequest(
                email,
                nickname
        );
    }
}
