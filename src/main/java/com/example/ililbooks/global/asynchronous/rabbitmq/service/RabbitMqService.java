package com.example.ililbooks.global.asynchronous.rabbitmq.service;

import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessagePromotionRequest;
import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessageOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {
    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    private final RabbitTemplate rabbitTemplate;

    /**
     * Producer
     *
     * @param messageOrderRequest (유저 메일, 유저 닉네임, 주문 번호, 총 가격)
     */
    public void sendOrderMessage(MessageOrderRequest messageOrderRequest) {
        //producer(메세지 수신)
        rabbitTemplate.convertAndSend(exchange, "order.mail", messageOrderRequest);
    }

    /**
     * Producer
     *
     * @param messagePromotionRequest (유저 메일, 유저 닉네임)
     */
    public void sendPromotionMessage(MessagePromotionRequest messagePromotionRequest) {
        rabbitTemplate.convertAndSend(exchange, "promotion.mail", messagePromotionRequest);
    }
}
