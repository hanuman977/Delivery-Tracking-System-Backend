package com.bits.delivery_service.repository;

import com.bits.delivery_service.entity.PacketEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacketEventRepository extends JpaRepository<PacketEvent, Long> {
    List<PacketEvent> findByTrackingId(String trackId);
}
