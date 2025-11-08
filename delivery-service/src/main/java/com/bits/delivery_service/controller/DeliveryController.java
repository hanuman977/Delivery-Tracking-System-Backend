package com.bits.delivery_service.controller;

import com.bits.delivery_service.dto.ConsignmentDTO;
import com.bits.delivery_service.dto.PacketDTO;
import com.bits.delivery_service.dto.DeliveryRequest;
import com.bits.delivery_service.dto.TrackRequest;
import com.bits.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService manager;

    @PostMapping("/create")
    public ResponseEntity<PacketDTO> create(@RequestBody DeliveryRequest req) {
        return ResponseEntity.ok(manager.createDelivery(req));
    }

    @GetMapping("/hubs")
    public ResponseEntity<List<String>> getHubs() {
        return ResponseEntity.ok(manager.getHubs());
    }

    @GetMapping("/track/{trackId}")
    public ResponseEntity<TrackRequest> getPacketById(@PathVariable String trackId) {
        return ResponseEntity.ok(manager.getPacketById(trackId));
    }

    @GetMapping("/consignments/{hub}")
    public ResponseEntity<List<ConsignmentDTO>> getConsignmentsByHubAndDate(@PathVariable String hub, @RequestParam String date) {
        return ResponseEntity.ok(manager.getConsignmentsByHubAndDate(hub, date));
    }

    @GetMapping("/consignment/{id}")
    public ResponseEntity<ConsignmentDTO> getConsignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(manager.getConsignmentById(id));
    }

    @PutMapping("/consignment/{id}/update-status")
    ResponseEntity<Map<String,Object>> updateConsignmentStatus(@PathVariable Long id, @RequestParam String hubName, @RequestParam String status) {
        return ResponseEntity.of(Optional.ofNullable(manager.updateConsignmentStatus(id, hubName, status)));
    }

    @GetMapping("/get-all-packets")
    public ResponseEntity<List<PacketDTO>> getAll() {
        return ResponseEntity.ok(manager.getAllDeliveries());
    }

    @GetMapping("/get-all-consignments")
    public ResponseEntity<List<ConsignmentDTO>> getAllConsignments() {
        return ResponseEntity.ok(manager.getAllConsignments());
    }
}
