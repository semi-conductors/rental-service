package com.rentmate.service.rental.domain.dto;

import java.time.LocalDateTime;

public record ErrorResponse
        (String errorCode,
         String message,
         LocalDateTime time) { }
