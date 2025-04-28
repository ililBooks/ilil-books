package com.example.ililbooks.global.notification.dto.request;

import lombok.Builder;
import software.amazon.awssdk.services.ses.model.*;

public record SendRequest(
        String from,
        String to,
        String subject,
        String content
) {

    @Builder
    public SendRequest {

    }

    public SendEmailRequest toSendEmailRequest() {

        Destination destination = Destination.builder()
                .toAddresses(this.to())
                .build();

        Message message = Message.builder()
                .subject(createContent(this.subject()))
                .body(Body.builder().html(createContent(this.content())).build())
                .build();

        return SendEmailRequest.builder()
                .source(this.from())
                .destination(destination)
                .message(message)
                .build();
    }

    private Content createContent(String text) {
        return Content.builder()
                .charset("UTF-8")
                .data(text)
                .build();
    }
}
