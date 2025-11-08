package com.bits.delivery_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryRequest {
    private String sender;
    private String senderEmail;
    private String receiver;
    private String receiverEmail;
    private String source;
    private String destination;
}
