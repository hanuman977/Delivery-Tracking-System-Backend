package com.bits.delivery_service.mapper;

import com.bits.delivery_service.dto.KafkaEvent;
import com.bits.delivery_service.entity.Packet;

public class KafkaEventMapper {
    public static KafkaEvent toKafkaEvent(Packet packet) {
        KafkaEvent event = new KafkaEvent();
        event.setTrackingId(packet.getTrackingId());
        event.setSender(packet.getSender());
        event.setSenderEmail(packet.getSenderEmail());
        event.setReceiver(packet.getReceiver());
        event.setReceiverEmail(packet.getReceiverEmail());
        event.setSource(packet.getSource());
        event.setDestination(packet.getDestination());
        event.setCurrentHub(packet.getCurrentHub());
        event.setCreatedAt(packet.getCreatedAt());
        event.setUpdatedAt(packet.getUpdatedAt());
        event.setStatus(packet.getStatus().name());
        return event;
    }
}
