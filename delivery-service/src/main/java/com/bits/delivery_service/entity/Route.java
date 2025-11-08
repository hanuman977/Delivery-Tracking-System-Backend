package com.bits.delivery_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    @Id
    private String id;
    private String routeName;
    @OneToMany(mappedBy = "route")
    private List<Consignment> consignments;
    @Lob
    private String hubsJson;
    @Lob
    private String hubArrivalTimesJson;
}
