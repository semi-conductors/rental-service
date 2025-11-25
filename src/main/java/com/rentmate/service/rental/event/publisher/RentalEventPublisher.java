package com.rentmate.service.rental.event.publisher;

import com.rentmate.service.rental.domain.entity.Rental;

import java.util.Map;

public interface RentalEventPublisher {
     void publishEvent(String exchange,String routingKey, Map<String, Object> payload);
    void publishDeliveryCostRequestEvent(Rental rental);
    void publishDeliveryReturnRequestEvent(Rental rental);
   void publishDeliveryLateReturnEvent(Rental rental);
    void publishLateReturnPaymentEvent(Rental rental);
     void publishPaymentEvent(Rental rental);
    void publishRefundEvent(Rental rental);
    void publishDeliveryRequestEvent(Rental rental);
    void publishRentalRejectedEvent(Long renterId);
    void publishRentalApprovedEvent(Long renterId);
    void publishRentalCreatedEvent(Long ownerId);

}
