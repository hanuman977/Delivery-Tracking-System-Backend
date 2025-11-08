package com.bits.delivery_service.dto;

import com.bits.delivery_service.entity.PacketEvent;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackRequest {
    private String trackingId;
    private String consignmentId;
    private String sender;
    private String receiver;
    private String source;
    private String currentHub;
    private String destination;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private List<PacketEvent> updates;
}
