package com.samuel.algadelivery.delivery.tracking.domain.service;

import com.samuel.algadelivery.delivery.tracking.domain.model.ContactPoint;

public interface DeliveryTimeEstimationService {
    DeliverEstimate estimate(ContactPoint sender, ContactPoint receiver);
}
