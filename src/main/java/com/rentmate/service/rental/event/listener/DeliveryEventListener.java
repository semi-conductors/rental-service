package com.rentmate.service.rental.event.listener;

import com.rentmate.service.rental.domain.dto.DeliveryEvent;
import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.Status;
import com.rentmate.service.rental.event.publisher.RentalEventPublisher;
import com.rentmate.service.rental.repository.RentalRepository;
import com.rentmate.service.rental.shared.exception.InvalidStatusTransitionException;
import com.rentmate.service.rental.shared.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeliveryEventListener {
    private final RentalRepository rentalRepository;
    private final RentalEventPublisher rentalEventPublisher;
    @Transactional(rollbackOn = Exception.class)
    @RabbitListener(queues = "delivery.events.queue")
    public void handleDeliveryEvent(DeliveryEvent event){
        validateEvent(event);
        Rental rental = fetchRental(event.getRentalId());
        switch (event.getEventType()){
            case "delivery.deliveryCost":
                handleDeliveryCost(rental,event);
                break;
            case "delivery.delivered":
                rental.setStatus(Status.Delivered);
                rentalRepository.save(rental);
                break;
            default:
                break;

        }

    }
    @Transactional(rollbackOn = Exception.class)
    @RabbitListener(queues = "delivery.return.events.queue")
    public void handleDeliveryReturnEvent(DeliveryEvent event){
        validateEvent(event);
        Rental rental = fetchRental(event.getRentalId());
        switch (event.getEventType()){
            case"delivery.returned":
                handleSuccessfulReturn(rental);
                break;
            case "delivery.inReturning":
                handleInReturningStatus(rental);
                break;
            default:
                break;
        }

    }
    private void handleDeliveryCost(Rental rental,DeliveryEvent event){
        if (rental.getStatus() != Status.Approved) {
            throw new InvalidStatusTransitionException(
                    "Cannot process delivery event for rental in status: " + rental.getStatus());
        }
        BigDecimal deliveryCost = event.getDeliveryCost();
        rental.setTotalPrice(rental.getTotalPrice().add(deliveryCost));
        rentalRepository.save(rental);
       rentalEventPublisher.publishPaymentEvent(rental);
    }
    private void handleSuccessfulReturn(Rental rental){
        rental.setStatus(Status.Returned);
        rentalRepository.save(rental);
       rentalEventPublisher.publishRefundEvent(rental);
    }

    private void handleInReturningStatus(Rental rental) {
        rental.setStatus(Status.InReturning);
        rentalRepository.save(rental);
    }

    private Rental fetchRental(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found with id: " + rentalId));
    }

    private void validateEvent(DeliveryEvent event) {
        if (event.getEventType() == null || event.getRentalId() == null) {
            throw new IllegalArgumentException("Invalid event payload");
        }
    }



}
