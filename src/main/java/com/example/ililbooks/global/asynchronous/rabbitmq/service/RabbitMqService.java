package com.example.ililbooks.global.asynchronous.rabbitmq.service;

import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMqService {
    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    /**
     * producer
     *
     * @param messageRequest (유저 메일, 유저 닉네임, 주문 번호, 총 가격)
     */
    public void send(MessageRequest messageRequest) {
        //producer(메세지 수신)
        rabbitTemplate.convertAndSend(exchange, routingKey, messageRequest);
    }
}
