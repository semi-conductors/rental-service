package com.rentmate.service.rental.event.publisher.impl;

import com.rentmate.service.rental.config.RabbitConfig;
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
    private final  String exchange = RabbitConfig.RENTAL_EXCHANGE;
    public static final String RENTAL_CREATED = "rental.created";
    public static final String RENTAL_DELIVERY_COST = "rental.cost.requested";

    public static final String RENTAL_APPROVED = "rental.approved";
    public static final String RENTAL_REJECTED = "rental.rejected";
    public static final String RENTAL_REFUND = "rental.refund";
    public static final String RENTAL_RETURN_REQUESTED = "rental.return.requested";
    public static final String RENTAL_DELIVERY_REQUESTED = "rental.delivery.requested";
    public static final String RENTAL_RETURN_LATE = "rental.return.late";
    public static final String RENTAL_LATE_RETURN = "rental.lateReturn";

    public void publishEvent(String routingKey, Map<String, Object> payload) {
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
    public void publishRentalCreatedEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                                    .withStatus(rental.getStatus())
                                    .build();
        publishEvent(RENTAL_CREATED,payLoad);
    }

    @Override
    public void publishDeliveryCostRequestEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.cost.requested")
                .build();
        publishEvent(RENTAL_DELIVERY_COST,payLoad);
        log.info("Publishing rental.cost.requested event for rental ID: {}", rental.getId());

    }

    @Override
    public void publishDeliveryReturnRequestEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.return.requested")
                .build();
        publishEvent(RENTAL_RETURN_REQUESTED,payLoad);
        log.info("Publishing rental.return.requested event for rental ID: {}", rental.getId());

    }

    @Override
    public void publishDeliveryLateReturnEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.return.late")
                .build();
        publishEvent(RENTAL_RETURN_LATE,payLoad);
        log.info("Publishing rental.return.late event for rental ID: {}", rental.getId());


    }

    @Override
    public void publishLateReturnPaymentEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                                      .withEventType("rental.lateReturn")
                                       .withDepositAmount(rental.getDepositAmount())
                                       .build();
        publishEvent(RENTAL_LATE_RETURN,payLoad);

    }

    @Override
    public void publishPaymentEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.approved")
                .withDepositAmount(rental.getDepositAmount())
                .withTotalPrice(rental.getTotalPrice())
                .build();
        publishEvent(RENTAL_APPROVED,payLoad);
        log.info("Publishing rental.approved event for rental ID: {}", rental.getId());



    }

    @Override
    public void publishRefundEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.refund")
                .withDepositAmount(rental.getDepositAmount())
                .build();
        publishEvent(RENTAL_REFUND,payLoad);

    }

    @Override
    public void publishDeliveryRequestEvent(Rental rental) {
        Map<String,Object> payLoad = EventPayloadBuilder.create(rental)
                .withEventType("rental.delivery.requested")
                .withStartDate(rental.getStartDate())
                .withEndDate(rental.getEndDate())
                .build();
        publishEvent(RENTAL_DELIVERY_REQUESTED,payLoad);
        log.info("Publishing rental.delivery.requested event for rental ID: {}", rental.getId());

    }

    public static class EventPayloadBuilder {
        private final Map<String, Object> payload = new HashMap<>();

         public static EventPayloadBuilder create(Rental rental){
             EventPayloadBuilder builder = new EventPayloadBuilder();
             builder.payload.put("rentalId",rental.getId());
             builder.payload.put("renterId",rental.getRenterId());
             builder.payload.put("ownerId",rental.getOwnerId());
             builder.payload.put("itemId",rental.getItemId());
             return builder;
         }
         public EventPayloadBuilder withStatus(Status status){
             if(status != null){
                 payload.put("status",status.name());
             }
             return this;
         }
        public EventPayloadBuilder withTotalPrice(BigDecimal totalPrice){
            if(totalPrice != null){
                payload.put("totalPrice",totalPrice);
            }
            return this;
        }
        public EventPayloadBuilder withDepositAmount(BigDecimal depositAmount){
            if(depositAmount != null){
                payload.put("depositAmount",depositAmount);
            }
            return this;
        }
        public EventPayloadBuilder withEventType(String eventType) {
            if (eventType != null) {
                payload.put("eventType", eventType);
            }
            return this;
        }
        public EventPayloadBuilder withStartDate(LocalDateTime startDate){
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
        public Map<String, Object> build() {
            return payload;
        }
    }

}
