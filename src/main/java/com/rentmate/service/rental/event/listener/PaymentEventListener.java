package com.rentmate.service.rental.event.listener;

import com.rentmate.service.rental.client.ItemServiceClient;
import com.rentmate.service.rental.domain.dto.PaymentEvent;
import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.PaymentEventType;
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

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentEventListener {
    private final RentalRepository rentalRepository;
    private final RentalEventPublisher rentalEventPublisher;
    private final RabbitTemplate rabbitTemplate;

   @RabbitListener(queues = "payment.events.queue")
    @Transactional(rollbackOn = Exception.class)
    public void handlePaymentEvent(PaymentEvent event){
         validateEvent(event);
        Rental rental= fetchRental(event.getRentalId());
        if (rental.getStatus() != Status.Approved) {
            throw new InvalidStatusTransitionException(
                    "Cannot process payment event for rental in status: " + rental.getStatus());
        }
        switch (event.getEventType()){
            case PAYMENT_PAID-> handlePaymentSuccess(rental);

            case PAYMENT_FAILED-> handlePaymentFailed(rental);

            default->{}

        }
    }
   @RabbitListener(queues = "payment.return.events.queue")
    @Transactional(rollbackOn = Exception.class)
    public void handlePaymentRefundEvent(PaymentEvent event){
        validateEvent(event);
        Rental rental = fetchRental(event.getRentalId());
       if (Objects.requireNonNull(event.getEventType()) == PaymentEventType.REFUNDED) {
           handleSuccessRefund(rental);
       }

    }

    @Transactional(rollbackOn = Exception.class)
    private void handlePaymentSuccess(Rental rental){
        rental.setStatus(Status.Paid);
        rentalRepository.save(rental);
        rentalEventPublisher.publishDeliveryRequestEvent(rental);
    }

    @Transactional(rollbackOn = Exception.class)
    private void handlePaymentFailed(Rental rental){
        rental.setStatus(Status.PaymentFailed);
        rentalRepository.save(rental);
    }
    @Transactional(rollbackOn = Exception.class)
    private void handleSuccessRefund(Rental rental){
        rental.setStatus(Status.Completed);
        rentalRepository.save(rental);
    }

    private Rental fetchRental(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found with id: " + rentalId));
    }

    private void validateEvent(PaymentEvent event) {
        if (event.getEventType() == null || event.getRentalId() == null) {
            throw new IllegalArgumentException("Invalid event payload");
        }
    }

}
