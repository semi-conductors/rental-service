package com.rentmate.service.rental.shared.exception;

public class InvalidRentalPeriodException extends RuntimeException {
    public InvalidRentalPeriodException(String message) {
        super(message);
    }
}
