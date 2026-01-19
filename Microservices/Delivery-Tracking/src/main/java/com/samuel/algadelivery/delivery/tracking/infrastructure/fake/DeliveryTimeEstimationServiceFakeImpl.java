package com.samuel.algadelivery.delivery.tracking.infrastructure.fake;

import com.samuel.algadelivery.delivery.tracking.domain.model.ContactPoint;
import com.samuel.algadelivery.delivery.tracking.domain.service.DeliverEstimate;
import com.samuel.algadelivery.delivery.tracking.domain.service.DeliveryTimeEstimationService;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DeliveryTimeEstimationServiceFakeImpl implements DeliveryTimeEstimationService {

    @Override
    public DeliverEstimate estimate(ContactPoint sender, ContactPoint receiver) {
        return new DeliverEstimate(
                Duration.ofHours(3),
                3.1
        );
    }
}
