package com.bits.delivery_service.entity;

import com.bits.delivery_service.utility.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Packet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String trackingId;
    @ManyToOne()
    @JoinColumn(name = "consignment_id", referencedColumnName = "id")
    private Consignment consignment;
    private String sender;
    private String senderEmail;
    private String receiver;
    private String receiverEmail;
    private String source;
    private String destination;
    private String currentHub;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    private boolean isLoaded;
}
