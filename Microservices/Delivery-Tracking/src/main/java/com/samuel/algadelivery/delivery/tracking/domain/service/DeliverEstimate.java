package com.samuel.algadelivery.delivery.tracking.domain.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@Getter
@AllArgsConstructor
public class DeliverEstimate {
    private Duration estimatedTime;
    private Double distanceInKm;
}
