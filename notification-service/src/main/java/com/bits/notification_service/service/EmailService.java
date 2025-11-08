package com.bits.notification_service.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final SesClient sesClient;

    @Value("${AWS_SES_SENDER}")
    private String sender;

    public void sendEmail(String to, String subject, String body) {
        try {
            Destination destination = Destination.builder().toAddresses(to).build();
            Message msg = Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder().text(Content.builder().data(body).build()).build())
                    .build();

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(sender)
                    .destination(destination)
                    .message(msg)
                    .build();

            sesClient.sendEmail(request);
            log.debug("Email sent to " + to);
        } catch (Exception e) {
            log.error("Failed to send email: " + e.getMessage());
        }
    }
}
