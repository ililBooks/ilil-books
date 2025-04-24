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
    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String queueName;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    /**
     * producer가 데이터를 보낼 Queue 선언 (주문 관련 데이터를 담는 Queue)
     */
    @Bean
    public Queue queue() {
        return new Queue(queueName);
    }

    /**
     * direcrt, topic, .., fanout 중 direct 교환소 선언
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchange);
    }

    /**
     * 주어진 queue와 exchange를 바인딩 (교환소에 큐 등록)
     * 여러 queue가 있다면 routingKey를 통해 어떤 queue에 보낼지 선택할 수 있다.
     *
     * 만약, queue에 해당하는 routingkey가 아니라면 보내지지 않는다.
     */
    @Bean
    public Binding binding(Queue queue, DirectExchange directExchange) {
        return BindingBuilder.bind(queue).to(directExchange).with(routingKey);
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
