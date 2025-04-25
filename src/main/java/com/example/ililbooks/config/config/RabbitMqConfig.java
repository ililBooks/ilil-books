package com.example.ililbooks.config.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    /**
     * 주문 관련 데이터를 담는 Queue
     */
    @Bean
    public Queue orderQueue() {
        return new Queue("order-mail-queue");
    }

    /**
     * promotion 관련 데이터를 담는 Queue
     */
    @Bean
    public Queue promotionQueue() {
        return new Queue("promotion-mail-queue");
    }

    /**
     * direct 교환소 선언
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchange);
    }

    /**
     * exchange에 주문 queue 등록 (routingKey: order.mail)
     */
    @Bean
    public Binding orderQueueBinding(Queue orderQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(orderQueue).to(directExchange).with("order.mail");
    }

    /**
     * exchange에 프로모션 queue 등록 (routingKey: promotion.mail)
     */
    @Bean
    public Binding promotionQueueBinding(Queue promotionQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(promotionQueue).to(directExchange).with("promotion.mail");
    }

    /**
     * RabbitTemplate
     * ConnectionFactory 로 연결 후 실제 작업을 위한 Template
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * 직렬화(메세지를 JSON 으로 변환하는 Message Converter)
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
