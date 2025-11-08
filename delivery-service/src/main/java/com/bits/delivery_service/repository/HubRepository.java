package com.bits.delivery_service.repository;


import com.bits.delivery_service.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HubRepository extends JpaRepository<Hub, Long> {
    Optional<Hub> findByNameIgnoreCase(String name);
}
