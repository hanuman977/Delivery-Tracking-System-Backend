package com.bits.delivery_service.mapper;

import com.bits.delivery_service.dto.RouteDTO;
import com.bits.delivery_service.entity.Route;
import org.json.JSONArray;

public class RouteMapper {
    public static RouteDTO toDTO(Route route) {
        JSONArray hubs = new JSONArray(route.getHubsJson());
        return new RouteDTO(
            route.getId(),
            hubs.get(0).toString(),
            hubs.get(hubs.length() - 1).toString(),
            hubs.toString()
        );
    }
}
