package com.bits.delivery_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaEvent {
    private Long id;
    private String trackingId;
    private String sender;
    private String senderEmail;
    private String receiver;
    private String receiverEmail;
    private String source;
    private String destination;
    private String currentHub;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
}
