package com.rentmate.service.rental.event.publisher.impl;
import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.Status;
import com.rentmate.service.rental.event.publisher.RentalEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class RentalEventPublisherImpl implements RentalEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String DELIVERY_EXCHANGE="delivery.exchange";
    private final String PAYMENT_EXCHANGE="payment.exchange";
    private final String NOTIFICATION_EXCHANGE="notification-exchange";
    private static final String RENTAL_DELIVERY_COST = "delivery.costRequested";

    private static final String RENTAL_APPROVED = "payment.request";
    private static final String RENTAL_REFUND =  "payment.request";
    private static final String RENTAL_RETURN_REQUESTED = "delivery.returnRequested";
    private static final String RENTAL_DELIVERY_REQUESTED = "delivery.deliveryRequested";
    private static final String RENTAL_RETURN_LATE = "delivery.lateReturn";
    private static final String RENTAL_LATE_RETURN =  "payment.response";
    private final String NOTIFICATION_KEY="notification.key";


    public void publishEvent(String exchange,String routingKey, Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            log.info("Published event {} for rental ID: {}", routingKey, payload.get("rentalId"));
        } catch (Exception e) {
            log.error("Failed to publish event {} for rental ID: {}. Error: {}",
                    routingKey, payload.get("rentalId"), e.getMessage(), e);
            throw new RuntimeException("Failed to publish event: " + routingKey, e);
        }
    }


    @Override
    public void publishDeliveryCostRequestEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.cost.requested")
                .withItemId(rental.getItemId())
                .withOwnerAddress(rental.getOwnerAddress())
                .withRenterAddress(rental.getRenterAddress())
                .build();
        publishEvent(DELIVERY_EXCHANGE,RENTAL_DELIVERY_COST,payLoad);
        log.info("Publishing rental.cost.requested event for rental ID: {}", rental.getId());

    }

    @Override
    public void publishDeliveryReturnRequestEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.return.requested")
                .withItemId(rental.getItemId())
                .withOwnerAddress(rental.getOwnerAddress())
                .withRenterAddress(rental.getRenterAddress())
                .build();
        publishEvent(DELIVERY_EXCHANGE,RENTAL_RETURN_REQUESTED,payLoad);
        log.info("Publishing rental.return.requested event for rental ID: {}", rental.getId());

    }

    @Override
    public void publishDeliveryLateReturnEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.return.late")
                .withItemId(rental.getItemId())
                .withOwnerAddress(rental.getOwnerAddress())
                .withRenterAddress(rental.getRenterAddress())
                .build();
        publishEvent(DELIVERY_EXCHANGE,RENTAL_RETURN_LATE,payLoad);
        log.info("Publishing rental.return.late event for rental ID: {}", rental.getId());

    }

    @Override
    public void publishLateReturnPaymentEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                                      .withEventType("rental.lateReturn")
                                       .withDepositAmount(rental.getDepositAmount())
                                       .build();
        publishEvent(PAYMENT_EXCHANGE,RENTAL_LATE_RETURN,payLoad);

    }

    @Override
    public void publishPaymentEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.approved")
                .withDepositAmount(rental.getDepositAmount())
                .withTotalPrice(rental.getTotalPrice())
                .build();
        publishEvent(PAYMENT_EXCHANGE,RENTAL_APPROVED,payLoad);
        log.info("Publishing rental.approved event for rental ID: {}", rental.getId());
    }

    @Override
    public void publishRefundEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.refund")
                .withDepositAmount(rental.getDepositAmount())
                .build();
        publishEvent(PAYMENT_EXCHANGE,RENTAL_REFUND,payLoad);
        log.info("published refund event");

    }

    @Override
    public void publishDeliveryRequestEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.delivery.requested")
                .withStartDate(rental.getStartDate().toString())
                .withOwnerAddress(rental.getOwnerAddress())
                .withRenterAddress(rental.getRenterAddress())
                .build();
        publishEvent(DELIVERY_EXCHANGE,RENTAL_DELIVERY_REQUESTED,payLoad);
        log.info("Publishing rental.delivery.requested event for rental ID: {}", rental.getId());

    }

    @Override
    public void publishRentalRejectedEvent(Long renterId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", renterId);
        payload.put("eventType", "RENTAL_REJECTED");
        payload.put("params", Map.of(
                "message", "Your rental request has been rejected."
        ));
        publishEvent(NOTIFICATION_EXCHANGE,NOTIFICATION_KEY,payload);
    }

    @Override
    public void publishRentalApprovedEvent(Long renterId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", renterId);
        payload.put("eventType", "RENTAL_APPROVED");
        payload.put("params", Map.of(
                "message", "Your rental request has been approved. Delivery cost is now being processed."
        ));

        publishEvent(NOTIFICATION_EXCHANGE,NOTIFICATION_KEY,payload);

    }

    @Override
    public void publishRentalCreatedEvent(Long ownerId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", ownerId);
        payload.put("eventType", "RENTAL_CREATED");
        payload.put("params", Map.of(
                "message", "You have a new rental request waiting for your review."
        ));

        publishEvent(NOTIFICATION_EXCHANGE,NOTIFICATION_KEY,payload);

    }


    public static class EventPayloadBuilder {
        private final Map<String, Object> payload = new HashMap<>();


         public static EventPayloadBuilder create(Rental rental){
             EventPayloadBuilder builder = new EventPayloadBuilder();
             builder.payload.put("rentalId",rental.getId());
             builder.payload.put("renterId",rental.getRenterId());
             builder.payload.put("ownerId",rental.getOwnerId());
             return builder;
         }

        public EventPayloadBuilder withItemId(Long itemId){
            if(itemId != null){
                payload.put("itemId",itemId);
            }
            return this;
        }
         public EventPayloadBuilder withStatus(Status status){
             if(status != null){
                 payload.put("status",status.name());
             }
             return this;
         }
        public EventPayloadBuilder withTotalPrice(BigDecimal totalPrice){
            if(totalPrice != null){
                payload.put("amount",totalPrice);
            }
            return this;
        }
        public EventPayloadBuilder withDepositAmount(BigDecimal depositAmount){
            if(depositAmount != null){
                payload.put("insurance",depositAmount);
            }
            return this;
        }
        public EventPayloadBuilder withEventType(String eventType) {
            if (eventType != null) {
                payload.put("eventType", eventType);
            }
            return this;
        }
        public EventPayloadBuilder withStartDate(String startDate){
             if(startDate!=null){
                 payload.put("startDate",startDate);
             }
             return this;
        }
        public EventPayloadBuilder withEndDate(LocalDateTime endDate){
            if(endDate!=null){
                payload.put("endDate",endDate);
            }
            return this;
        }
        public EventPayloadBuilder withOwnerAddress(String ownerAddress){
            if(ownerAddress!=null){
                payload.put("ownerAddress",ownerAddress);
            }
            return this;
        }
        public EventPayloadBuilder withRenterAddress(String renterAddress){
            if(renterAddress!=null){
                payload.put("renterAddress",renterAddress);
            }
            return this;
        }
        public EventPayloadBuilder withMessage(String message){
            if(message!=null){
                payload.put("message",message);
            }
            return this;
        }
        public Map<String, Object> build() {
            return payload;
        }


    }

}
