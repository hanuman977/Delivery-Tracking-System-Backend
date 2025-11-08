package com.bits.delivery_service.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsignmentDTO {
    private Long id;
    private RouteDTO route;
    private LocalDate tripDate;
    private String currentHub;
    private int activePacketsCount;
    private int newPacketsCount;
    private String status;
}
