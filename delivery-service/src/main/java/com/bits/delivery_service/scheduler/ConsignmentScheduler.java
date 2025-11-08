package com.bits.delivery_service.scheduler;

import com.bits.delivery_service.entity.*;
import com.bits.delivery_service.repository.*;
import com.bits.delivery_service.utility.DeliveryStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsignmentScheduler {
    private final RouteRepository routeRepository;
    private final ConsignmentRepository consignmentRepository;
    private final PacketRepository packetRepository;
    private final PacketEventRepository packetEventRepository;
    private final ConsignmentEventRepository consignmentEventRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyConsignment() {
        LocalDate today = LocalDate.now();
        List<Route> routes = routeRepository.findAll();

        for (Route route : routes) {
            boolean exists = consignmentRepository.existsByRouteAndTripDate(route, today);
            if (exists) {
                continue;
            }
            Consignment consignment = new Consignment();
            consignment.setRoute(route);
            consignment.setTripDate(today);
            consignment.setStatus("SCHEDULED");
            consignmentRepository.save(consignment);
            consignmentEventRepository.save(new ConsignmentEvent(consignment.getId(), null, consignment.getStatus(), LocalDateTime.now()));
            log.info("Created New RouteTrip for {} on {}", route.getRouteName(), today);
        }
    }

    @Scheduled(cron = "0 */2 * * * *")
    public void assignUnassignedDeliveries() {
        List<Packet> unassignedDeliveries = packetRepository.findAllByConsignmentIsNull();

        for (Packet packet : unassignedDeliveries) {
            try {
                assignToConsignment(packet);
                packetEventRepository.save(new PacketEvent(packet.getId(), packet.getTrackingId(), packet.getCurrentHub(), packet.getStatus().name(), LocalDateTime.now()));
            } catch (Exception e) {
                log.warn("Failed to assign delivery {}: {}", packet.getId(), e.getMessage());
            }
        }
    }

    private void assignToConsignment(Packet packet) {
        Route route = this.getRoute(packet.getSource(), packet.getDestination());
        Map<String,String> arrivalTimes = parseHubTimes(route.getHubArrivalTimesJson());
        String sourceTimeStr = arrivalTimes.get(packet.getSource());
        if (sourceTimeStr == null) throw new RuntimeException("No arrival time for source on route");

        LocalDateTime createdAt = packet.getCreatedAt();
        LocalTime sourceCutoff = LocalTime.parse(sourceTimeStr);
        System.out.printf("Source Cutoff: %s, CreatedAt: %s\n", sourceCutoff, createdAt.toLocalTime());
        LocalDate tripDate = createdAt.toLocalTime().isBefore(sourceCutoff) ? LocalDate.now() : LocalDate.now().plusDays(1);

        Consignment consignment = consignmentRepository
                .findByRouteAndTripDate(route, tripDate)
                .orElseGet(() -> createTrip(route, tripDate));

        packet.setStatus(DeliveryStatus.ASSIGNED);
        packet.setConsignment(consignment);
        packet.setUpdatedAt(LocalDateTime.now());
        packetRepository.save(packet);

        log.info("Assigned Delivery {} to RouteTrip {} ({})",
                packet.getId(), consignment.getId(), consignment.getTripDate());
    }

    private Map<String,String> parseHubTimes(String json) {
        try { return mapper.readValue(json, new TypeReference<Map<String,String>>() {}); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    private Route getRoute(String source, String destination) {
        return routeRepository.findAll().stream()
                .filter(r -> containsPath(r.getHubsJson(), source, destination))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No route available for source-destination"));
    }

    private boolean containsPath(String hubsJson, String source, String destination) {
        try {
            List<String> hubs = mapper.readValue(hubsJson, new TypeReference<List<String>>() {});
            return hubs.contains(source) && hubs.contains(destination) && hubs.indexOf(source) < hubs.indexOf(destination);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Consignment createTrip(Route route, LocalDate tripDate) {
        log.debug("Creating a New Consignment for Tomorrow");
        Consignment consignment = new Consignment();
        consignment.setRoute(route);
        consignment.setTripDate(tripDate);
        consignment.setStatus("SCHEDULED");

        consignment = consignmentRepository.save(consignment);
        consignmentEventRepository.save(new ConsignmentEvent(consignment.getId(), null, consignment.getStatus(), LocalDateTime.now()));
        return consignment;
    }
}
