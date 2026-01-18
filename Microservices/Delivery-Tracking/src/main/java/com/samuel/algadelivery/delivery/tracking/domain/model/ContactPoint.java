package com.samuel.algadelivery.delivery.tracking.domain.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContactPoint { //nao pode ter seu estado alterado
    private String zipCode;
    private String street;
    private String number;
    private String complement;
    private String name;
    private String phone;
}
