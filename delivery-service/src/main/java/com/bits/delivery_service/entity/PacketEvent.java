package com.bits.delivery_service.entity;

import com.bits.delivery_service.utility.DeliveryStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacketEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long packetId;
    private String trackingId;
    private String currentHub;
    private String status;
    private LocalDateTime updatedAt;

    public PacketEvent(Long packetId, String trackingId, String currentHub, String status, LocalDateTime updatedAt) {
        this.packetId = packetId;
        this.trackingId = trackingId;
        this.currentHub = currentHub;
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
