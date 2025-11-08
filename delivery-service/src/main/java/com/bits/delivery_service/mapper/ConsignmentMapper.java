package com.bits.delivery_service.mapper;

import com.bits.delivery_service.dto.ConsignmentDTO;
import com.bits.delivery_service.entity.Consignment;

public class ConsignmentMapper {
    public static ConsignmentDTO toDTO(Consignment consignment) {
        return new ConsignmentDTO(
                consignment.getId(),
                consignment.getRoute() != null ? RouteMapper.toDTO(consignment.getRoute()): null,
                consignment.getTripDate(),
                consignment.getCurrentHub(),
                0, 0,
                consignment.getStatus()
        );
    }

    public static Consignment toEntity(ConsignmentDTO consignmentDTO) {
        Consignment consignment = new Consignment();
        consignment.setId(consignmentDTO.getId());
        consignment.setTripDate(consignmentDTO.getTripDate());
        consignment.setCurrentHub(consignmentDTO.getCurrentHub());
        consignment.setStatus(consignmentDTO.getStatus());
        return consignment;
    }
}
