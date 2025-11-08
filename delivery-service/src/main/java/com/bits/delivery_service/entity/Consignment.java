package com.bits.delivery_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    private Route route;
    private LocalDate tripDate;
    private String status;
    private String currentHub;
    @OneToMany(mappedBy = "consignment")
    private List<Packet> items;
}
