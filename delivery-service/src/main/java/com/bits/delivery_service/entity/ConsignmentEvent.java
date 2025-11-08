package com.bits.delivery_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsignmentEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long consignmentId;
    private String hub;
    private String status;
    private LocalDateTime updatedAt;

    public ConsignmentEvent(Long consignmentId, String hub, String status, LocalDateTime updatedAt) {
        this.consignmentId = consignmentId;
        this.hub = hub;
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
