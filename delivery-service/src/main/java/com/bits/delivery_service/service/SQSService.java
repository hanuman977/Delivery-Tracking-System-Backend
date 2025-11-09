package com.bits.delivery_service.service;

import com.bits.delivery_service.dto.KafkaEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@RequiredArgsConstructor
@Service
public class SQSService {
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${SQS_QUEUE1_URL}")
    private String queueUrl1;

    @Value("${SQS_QUEUE2_URL}")
    private String queueUrl2;

    public void sendToQueues(KafkaEvent event, String queue) throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(event);
        if(queue.equals("PACKET_CREATED")) {
            sendMessage(queueUrl1, message);
        }
        else if(queue.equals("STATUS_UPDATED")) {
            sendMessage(queueUrl2, message);
        }
    }

    private void sendMessage(String queueUrl, String message) {
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build());
        log.debug("Sent message to: " + queueUrl);
    }

}
