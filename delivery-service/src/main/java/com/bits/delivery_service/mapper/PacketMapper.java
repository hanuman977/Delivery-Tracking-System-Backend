package com.bits.delivery_service.mapper;

import com.bits.delivery_service.dto.PacketDTO;
import com.bits.delivery_service.entity.Packet;

public class PacketMapper {
    public static PacketDTO toDTO(Packet item) {
        return new PacketDTO(
                item.getId(),
                item.getTrackingId(),
                item.getConsignment() != null ? ConsignmentMapper.toDTO(item.getConsignment()) : null,
                item.getSender(),
                item.getSenderEmail(),
                item.getReceiver(),
                item.getReceiverEmail(),
                item.getSource(),
                item.getDestination(),
                item.getCurrentHub(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getStatus().name()
        );
    }
}
