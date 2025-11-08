package com.bits.notification_service.service;

import com.bits.notification_service.dto.KafkaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SQSListenerService {

    private final SqsClient sqsClient;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${SQS_QUEUE1_URL}")
    private String queueUrl1;

    @Value("${SQS_QUEUE2_URL}")
    private String queueUrl2;

    @PostConstruct
    public void startListening() {
        new Thread(() -> listenToQueue(queueUrl1)).start();
        new Thread(() -> listenToQueue(queueUrl2)).start();
    }

    private void listenToQueue(String queueUrl) {
        while (true) {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(5)
                    .waitTimeSeconds(20)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();

            for (Message message : messages) {
                try {
                    // Deserialize message body into KafkaEvent
                    KafkaEvent event = objectMapper.readValue(message.body(), KafkaEvent.class);
                    log.debug("Received event from " + queueUrl + ": " + event);

                    // Build email content dynamically
                    String subject = "Shipment Update - " + event.getTrackingId();
                    String body = String.format("""
                            Dear Customer,
                            
                            Your shipment from %s to %s is currently at hub: %s.
                            
                            Current status: %s
                            Sender: %s (%s)
                            Receiver: %s (%s)
                            
                            Last updated: %s
                            
                            Regards,
                            Logistics Team
                            """,
                            event.getSource(),
                            event.getDestination(),
                            event.getCurrentHub(),
                            event.getStatus(),
                            event.getSender(),
                            event.getSenderEmail(),
                            event.getReceiver(),
                            event.getReceiverEmail(),
                            event.getUpdatedAt()
                    );

                    // Send email to receiver
                    emailService.sendEmail(event.getReceiverEmail(), subject, body);
                    emailService.sendEmail(event.getSenderEmail(), subject, body);

                    // Delete message after successful processing
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build());

                } catch (Exception e) {
                    log.error("Failed to process message: " + e.getMessage());
                }
            }
        }
    }
}
