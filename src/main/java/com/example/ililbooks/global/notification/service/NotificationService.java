package com.example.ililbooks.global.notification.service;

import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static com.example.ililbooks.global.exception.ErrorMessage.FAILED_SEND_MAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender javaMailSender;

    /**
     * consumer
     *
     * queue에서 데이터를 가져와 메일 발송
     */
    @RabbitListener(queues = "${spring.rabbitmq.template.default-receive-queue}")
    public void sendOrderMail(MessageRequest messageRequest) {
        //텍스트로 메일 보내기
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        try {
            //메일을 받을 수신자 설정
            simpleMailMessage.setTo(messageRequest.email());

            //메일 제목
            simpleMailMessage.setSubject("주문이 완료되었습니다.");

            //메일 내용
            simpleMailMessage.setText(
                    "주문 정보\n" +
                    "-------------------------\n" +
                    "닉네임: " + messageRequest.nickName() + "\n" +
                    "주문 번호: " + messageRequest.orderNumber() + "\n" +
                    "총 가격: " + messageRequest.totalPrice() + "\n"
            );

            //메일 전송
            javaMailSender.send(simpleMailMessage);

        } catch (Exception e) {
            //메일 전송이 실패하더라도 주문은 성공
            log.error(FAILED_SEND_MAIL.getMessage(), e);
        }
    }
}
