package com.example.ililbooks.global.notification.service;

import com.example.ililbooks.global.notification.dto.request.SendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SesService {

    private final SesClient sesClient;
    private final Environment env;

    public void send(String subject, String content, List<String> emails) {
        SendRequest sendRequest = SendRequest.builder()
                .from(env.getProperty("spring.mail.username"))
                .subject(subject)
                .to(emails)
                .content(content)
                .build();

        sesClient.sendEmail(sendRequest.toSendEmailRequest());
    }
}
