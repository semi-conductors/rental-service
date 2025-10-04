package com.rentmate.service.rental.shared.exception;

public class ExpiredKeyException extends RuntimeException{
    public ExpiredKeyException(String message) {
        super(message);
    }
}
