package com.bits.delivery_service.repository;

import com.bits.delivery_service.entity.Consignment;
import com.bits.delivery_service.entity.Packet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacketRepository extends JpaRepository<Packet, Long>{
    List<Packet> findAllByConsignmentIsNull();

    @Query("SELECT p FROM Packet p WHERE p.consignment.id = :consignmentId AND p.isLoaded = false and p.source = :source")
    List<Packet> findNewPacketsByConsignment(Long consignmentId, String source);

    @Query("SELECT p FROM Packet p WHERE p.consignment.id = :consignmentId AND p.isLoaded = true AND p.status != 'DELIVERED'")
    List<Packet> findActivePacketsByConsignment(Long consignmentId, String hub);

    @Query("SELECT p FROM Packet p WHERE p.consignment.id = :consignmentId AND p.source = :source AND p.isLoaded = false")
    List<Packet> findNewPacketsByConsignmentIdAndSource(Long consignmentId, String source);

    Optional<Packet> findByTrackingId(String trackId);
}
