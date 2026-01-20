package com.samuel.algadelivery.courier.management.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class AssignedDelivery {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    private OffsetDateTime assignedAt;

    @ManyToOne(optional = false)
    @Setter(AccessLevel.PACKAGE)
    private Courier courier;

    static AssignedDelivery pending(UUID deliveryId, Courier courier){
        AssignedDelivery delivery = new AssignedDelivery();
        delivery.setId(deliveryId);
        delivery.setAssignedAt(OffsetDateTime.now());
        delivery.setCourier(courier);
        return delivery;
    }
}
