package com.bits.delivery_service.service;

import com.bits.delivery_service.dto.*;
import com.bits.delivery_service.entity.*;
import com.bits.delivery_service.mapper.ConsignmentMapper;
import com.bits.delivery_service.mapper.KafkaEventMapper;
import com.bits.delivery_service.mapper.PacketMapper;
import com.bits.delivery_service.mapper.RouteMapper;
import com.bits.delivery_service.repository.*;
import com.bits.delivery_service.utility.DeliveryStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final PacketRepository packetRepository;
    private final ConsignmentRepository consignmentRepository;
    private final RouteRepository routeRepository;
    private final ConsignmentEventRepository consignmentEventRepository;
    private final PacketEventRepository packetEventRepository;
    private final KafkaTemplate<String, KafkaEvent> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    public List<String> getHubs() {
        return routeRepository.findAll().stream()
            .flatMap(r -> {
                JSONArray arr = new JSONArray(r.getHubsJson());
                List<String> hubs = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) hubs.add(arr.getString(i));
                return hubs.stream();
            })
            .distinct()
            .sorted()
            .toList();
    }

    public PacketDTO createDelivery(DeliveryRequest req) {

        Packet item = new Packet();
        item.setTrackingId("CC"+ UUID.randomUUID().toString().substring(0,8).toUpperCase());
        item.setSender(req.getSender());
        item.setSenderEmail(req.getSenderEmail());
        item.setReceiver(req.getReceiver());
        item.setReceiverEmail(req.getReceiverEmail());
        item.setSource(req.getSource());
        item.setDestination(req.getDestination());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setStatus(DeliveryStatus.CREATED);
        item.setCurrentHub(req.getSource());
        item.setLoaded(false);
        packetRepository.save(item);

        KafkaEvent event = KafkaEventMapper.toKafkaEvent(item);
        kafka.send("PACKET_CREATED", event);
        packetEventRepository.save(new PacketEvent(item.getId(), item.getTrackingId(), item.getCurrentHub(), item.getStatus().name(), LocalDateTime.now()));
        return PacketMapper.toDTO(item);
    }

    public ConsignmentDTO getConsignmentById(Long id) {
        return consignmentRepository.findById(id).map(ConsignmentMapper::toDTO).orElse(null);
    }

    public Map<String, Object> updateConsignmentStatus(Long consignmentId, String hubName, String status) {
        ConsignmentDTO consignment = this.getConsignmentById(consignmentId);
        try {
            if (consignment == null) {
                return Map.of("status", "ERROR", "message", "Consignment not found");
            }

            Route route = routeRepository.findById(consignment.getRoute().getId()).orElse(null);
            if (route == null) {
                return Map.of("status", "ERROR", "message", "Route not found for this consignment");
            }

            List<String> hubs = new ObjectMapper().readValue(route.getHubsJson(), List.class);
            if (!hubs.contains(hubName)) {
                return Map.of("status", "BLOCKED", "message", hubName + " is not part of route " + route.getRouteName());
            }

            Optional<ConsignmentEvent> lastEventOpt =
                    consignmentEventRepository.findTopByConsignmentIdOrderByUpdatedAtDesc(consignmentId);
            String lastStatus = lastEventOpt.map(ConsignmentEvent::getStatus).orElse("SCHEDULED");
            String lastHub = lastEventOpt.map(ConsignmentEvent::getHub).orElse(null);

            int currentHubIndex = hubs.indexOf(hubName);
            int lastHubIndex = lastHub != null ? hubs.indexOf(lastHub) : -1;
            log.debug("Hubs Index - Current: {}, Last: {}", currentHubIndex, lastHubIndex);

            if (status.equalsIgnoreCase("ARRIVAL")) {

                // ARRIVAL cannot happen twice consecutively
                if (lastStatus.equalsIgnoreCase("ARRIVAL") && hubName.equalsIgnoreCase(lastHub)) {
                    return Map.of("status", "BLOCKED",
                            "message", "ARRIVAL already recorded for hub: " + hubName);
                }

                // First ARRIVAL must be at origin hub
                if (lastStatus.equalsIgnoreCase("SCHEDULED") && currentHubIndex != 0) {
                    return Map.of("status", "BLOCKED",
                            "message", "First ARRIVAL must be at origin hub: " + hubs.get(0));
                }

                if (lastStatus.equalsIgnoreCase("DEPARTURE")) {
                    // Ensure the ARRIVAL hub is immediately next in sequence
                    if (currentHubIndex != lastHubIndex + 1) {
                        return Map.of("status", "BLOCKED",
                                "message", "Cannot ARRIVE at " + hubName +
                                        " before DEPARTURE from previous hub: " + hubs.get(lastHubIndex));
                    }

                    // Ensure ARRIVAL for this hub has not already occurred
                    boolean alreadyArrived = consignmentEventRepository.existsByConsignmentIdAndHubAndStatusIgnoreCase(
                            consignmentId, hubName, "ARRIVAL");
                    if (alreadyArrived) {
                        return Map.of("status", "BLOCKED",
                                "message", "ARRIVAL already recorded earlier for hub: " + hubName);
                    }

                    // Ensure ARRIVAL has occurred at previous hub before DEPARTURE
                    if (lastHubIndex >= 0) {
                        boolean prevHubArrived = consignmentEventRepository.existsByConsignmentIdAndHubAndStatusIgnoreCase(
                                consignmentId, hubs.get(lastHubIndex), "ARRIVAL");
                        if (!prevHubArrived) {
                            return Map.of("status", "BLOCKED",
                                    "message", "Cannot ARRIVE at " + hubName +
                                            " since ARRIVAL never recorded at previous hub: " + hubs.get(lastHubIndex));
                        }
                    }
                }

                // ARRIVAL cannot happen before DEPARTURE from last hub
                if (lastStatus.equalsIgnoreCase("ARRIVAL")) {
                    return Map.of("status", "BLOCKED",
                            "message", "Cannot ARRIVE at " + hubName +
                                    " before DEPARTURE from " + lastHub);
                }
            }

            else if (status.equalsIgnoreCase("DEPARTURE")) {
                if (lastStatus.equalsIgnoreCase("SCHEDULED")) {
                    return Map.of("status", "BLOCKED",
                            "message", "Cannot DEPART before first ARRIVAL at origin hub");
                }

                // Cannot DEPART from hub before ARRIVAL there
                if (!hubName.equalsIgnoreCase(lastHub)) {
                    return Map.of("status", "BLOCKED",
                            "message", "Cannot DEPART from " + hubName +
                                    " before ARRIVAL at this hub");
                }

                // Cannot DEPART twice from same hub
                if (lastStatus.equalsIgnoreCase("DEPARTURE")) {
                    return Map.of("status", "BLOCKED",
                            "message", "DEPARTURE already recorded for hub: " + hubName);
                }
            }

            else {
                return Map.of("status", "FAILED", "message", "Invalid status. Must be ARRIVAL or DEPARTURE");
            }


            if (status.equals("DEPARTURE")) {
                if(!consignment.getRoute().getDestination().equals(hubName)) {
                    consignment.setStatus("IN_TRANSIT");
                    consignment.setCurrentHub(hubName);
                    List<PacketDTO> packets = this.getNewPacketsByConsignment(consignmentId, hubName);
                    for (PacketDTO packet : packets) {

                        this.updatePacketStatus(packet.getId(), DeliveryStatus.IN_TRANSIT, hubName);
                    }
                }
            }
            else {
                if (consignment.getRoute().getDestination().equals(hubName)) {
                    consignment.setStatus("DELIVERED");
                    consignment.setCurrentHub(hubName);
                    List<PacketDTO> packets = this.getActivePacketsByConsignment(consignmentId, hubName);
                    for (PacketDTO packet : packets) {
                        this.updatePacketStatus(packet.getId(), DeliveryStatus.DELIVERED, hubName);
                    }
                } else {
                    consignment.setStatus("IN_TRANSIT");
                    consignment.setCurrentHub(hubName);
                    List<PacketDTO> packets = this.getActivePacketsByConsignment(consignmentId, hubName);
                    for (PacketDTO packet : packets) {
                        this.updatePacketStatus(packet.getId(), DeliveryStatus.IN_TRANSIT, hubName);
                    }
                }
            }
            this.updateConsignment(consignmentId, consignment);

            ConsignmentEvent consignmentEvent = new ConsignmentEvent();
            consignmentEvent.setConsignmentId(consignmentId);
            consignmentEvent.setHub(hubName);
            consignmentEvent.setStatus(status);
            consignmentEvent.setUpdatedAt(LocalDateTime.now());
            consignmentEventRepository.save(consignmentEvent);

            return Map.of("consignmentId", consignmentId, "hub", hubName, "status", "OK");
        }
        catch (Exception e) {
            return Map.of("consignmentId", consignmentId, "hub", hubName, "status", "ERROR", "message", e.getMessage());
        }
    }

    public void updateConsignment(Long consignmentId, ConsignmentDTO consignmentDTO) {
        Optional<Consignment> consignment = consignmentRepository.findById(consignmentId);
        consignment.ifPresent(value -> {
            value.setStatus(consignmentDTO.getStatus());
            value.setCurrentHub(consignmentDTO.getCurrentHub());
            consignmentRepository.save(value);
        });
    }

    public List<PacketDTO> getAllDeliveries() {
        return packetRepository.findAll().stream()
                .map(PacketMapper::toDTO)
                .toList();
    }

    public List<ConsignmentDTO> getAllConsignments() {
        return consignmentRepository.findAll().stream()
                .map(ConsignmentMapper::toDTO)
                .toList();
    }

    public List<ConsignmentDTO> getConsignmentsByHubAndDate(String hub, String date) {
        List<Route> routes = routeRepository.findAll().stream()
                .filter(route -> route.getRouteName().contains(hub))
                .toList();
        List<ConsignmentDTO> consignments = new ArrayList<>();
        for (Route route : routes) {
            Optional<Consignment> consignment = consignmentRepository.findByRouteAndTripDate(route, LocalDate.parse(date));
            if(consignment.isPresent()) {
                int activePackets = packetRepository.findActivePacketsByConsignment(consignment.get().getId(), hub).size();
                int newPackets = packetRepository.findNewPacketsByConsignmentIdAndSource(consignment.get().getId(), hub).size();
                ConsignmentDTO consignmentDTO = new ConsignmentDTO();
                consignmentDTO.setActivePacketsCount(activePackets);
                consignmentDTO.setNewPacketsCount(newPackets);
                consignmentDTO.setId(consignment.get().getId());
                consignmentDTO.setRoute(RouteMapper.toDTO(route));
                consignmentDTO.setTripDate(consignment.get().getTripDate());
                consignmentDTO.setStatus(consignment.get().getStatus());
                consignmentDTO.setCurrentHub(consignment.get().getCurrentHub());
                consignmentDTO.setStatus(consignment.get().getStatus());
                consignments.add(consignmentDTO);
                break;
            }
        }
        return consignments;
    }

    public void updatePacketStatus(Long packetId, DeliveryStatus status, String hubName) {
        Optional<Packet> packet = packetRepository.findById(packetId);
        packet.ifPresent(value -> {
            value.setStatus(status);
            value.setCurrentHub(hubName);
            value.setUpdatedAt(LocalDateTime.now());
            if (status == DeliveryStatus.IN_TRANSIT) {
                value.setLoaded(true);
            }
            packetRepository.save(value);
            KafkaEvent event = KafkaEventMapper.toKafkaEvent(value);
            kafka.send("STATUS_UPDATED", event);

            PacketEvent packetEvent = new PacketEvent();
            packetEvent.setPacketId(value.getId());
            packetEvent.setTrackingId(value.getTrackingId());
            packetEvent.setCurrentHub(value.getCurrentHub());
            packetEvent.setStatus(value.getStatus().name());
            packetEvent.setUpdatedAt(LocalDateTime.now());
            packetEventRepository.save(packetEvent);
        });
    }

    public List<PacketDTO> getActivePacketsByConsignment(Long consignmentId, String hub) {
        return packetRepository.findActivePacketsByConsignment(consignmentId, hub).stream()
                .map(PacketMapper::toDTO)
                .toList();
    }

    public List<PacketDTO> getNewPacketsByConsignment(Long consignmentId, String hub) {
        return packetRepository.findNewPacketsByConsignment(consignmentId, hub).stream()
                .map(PacketMapper::toDTO)
                .toList();
    }

    public TrackRequest getPacketById(String trackId) {
        Optional<Packet> packet = packetRepository.findByTrackingId(trackId);
        if (packet.isEmpty()) {
            return null;
        }
        List<PacketEvent> events = packetEventRepository.findByTrackingId(trackId);
        TrackRequest trackRequest = new TrackRequest();
        trackRequest.setTrackingId(packet.get().getTrackingId());
        trackRequest.setSender(packet.get().getSender());
        trackRequest.setReceiver(packet.get().getReceiver());
        trackRequest.setSource(packet.get().getSource());
        trackRequest.setDestination(packet.get().getDestination());
        trackRequest.setCreatedAt(packet.get().getCreatedAt());
        trackRequest.setUpdatedAt(packet.get().getUpdatedAt());
        trackRequest.setStatus(packet.get().getStatus().toString());
        trackRequest.setUpdates(events);
        trackRequest.setCurrentHub(packet.get().getCurrentHub());
        trackRequest.setConsignmentId(packet.get().getConsignment() != null ? packet.get().getConsignment().getId().toString() : null);
        log.info("Packet Updates: {}", events.toString());
        return trackRequest;
    }
}
