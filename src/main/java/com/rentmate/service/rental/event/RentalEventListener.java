package com.rentmate.service.rental.event;

import com.rentmate.service.rental.client.ItemServiceClient;
import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.DeliveryMethode;
import com.rentmate.service.rental.domain.enumuration.Status;
import com.rentmate.service.rental.repository.RentalRepository;
import com.rentmate.service.rental.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalEventListener {
    private final RentalRepository rentalRepository;
    private final ItemServiceClient itemServiceClient;
    private final RabbitTemplate rabbitTemplate;

    //@RabbitListener(queues = "payment.events.queue")
    @Transactional
    public void handlePaymentEvent(Map<String,Object> event){
        String eventStatus =(String) event.get("eventStatus");
        if (eventStatus == null || !event.containsKey("rentalId")) {
        throw new IllegalArgumentException("Invalid event payload");
        }
        Long rentalId = Long.valueOf(event.get("rentalId").toString());
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found with id: " + rentalId));
        switch (eventStatus){
            case "payment.paid":
                handlePaymentSuccess(rental);
                break;
            case "payment.failed":
                handlePaymentFailed(rental);
                break;
            default:
                break;

        }

    }
    @Transactional
    private void handlePaymentSuccess(Rental rental){
        rental.setStatus(Status.Paid);
        rentalRepository.save(rental);
        itemServiceClient.updateAvailability(rental.getItemId(), rental.getStartDate(),rental.getEndDate());
        if(rental.getDeliveryMethode()== DeliveryMethode.Courier){
           Map<String,Object> deliveryEventPayload= Map.of(
                   "rentalId",rental.getId(),
                   "renterId",rental.getRenterId(),
                   "itemId",rental.getItemId(),
                   "ownerId",rental.getOwnerId()
           );
            rabbitTemplate.convertAndSend(
                    "rental.exchange",
                    "rental.deliveryRequested",
                    deliveryEventPayload
            );
        }

    }
    @Transactional
    private void handlePaymentFailed(Rental rental){
        rental.setStatus(Status.PaymentFailed);
        rentalRepository.save(rental);

        // Notify renter about failure
        rabbitTemplate.convertAndSend(
                "rental.exchange",
                "rental.statusChanged",
                Map.of("rentalId", rental.getId(), "status", rental.getStatus().name())
        );
    }

}
