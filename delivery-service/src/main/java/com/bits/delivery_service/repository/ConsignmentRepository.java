package com.bits.delivery_service.repository;

import com.bits.delivery_service.entity.Consignment;
import com.bits.delivery_service.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsignmentRepository extends JpaRepository<Consignment, Long> {
    @Query("SELECT c FROM Consignment c WHERE c.route = :route AND c.tripDate = :tripDate AND c.status <> 'DELIVERED'")
    Optional<Consignment> findByRouteAndTripDate(Route route, LocalDate tripDate);
    boolean existsByRouteAndTripDate(Route route, LocalDate tripDate);

    List<Consignment> findByRoute(Route route);
}
