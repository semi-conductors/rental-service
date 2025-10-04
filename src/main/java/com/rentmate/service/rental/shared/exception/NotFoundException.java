package com.rentmate.service.rental.shared.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String message) {

        super(message);
    }
}
