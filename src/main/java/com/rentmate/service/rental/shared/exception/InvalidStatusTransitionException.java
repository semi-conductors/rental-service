package com.rentmate.service.rental.shared.exception;

public class InvalidStatusTransitionException extends RuntimeException{
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
