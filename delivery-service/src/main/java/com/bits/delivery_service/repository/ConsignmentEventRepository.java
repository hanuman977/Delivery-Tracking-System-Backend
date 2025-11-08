package com.bits.delivery_service.repository;

import com.bits.delivery_service.entity.ConsignmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsignmentEventRepository extends JpaRepository<ConsignmentEvent, Long> {
    List<ConsignmentEvent> findByConsignmentIdOrderByUpdatedAtAsc(Long consignmentId);

    Optional<ConsignmentEvent> findTopByConsignmentIdOrderByUpdatedAtDesc(Long consignmentId);

    boolean existsByConsignmentIdAndHubAndStatusIgnoreCase(Long consignmentId, String hubName, String arrival);
}
