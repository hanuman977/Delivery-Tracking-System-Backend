package com.bits.delivery_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONArray;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteDTO {
    private String id;
    private String source;
    private String destination;
    private String hubs;

}
