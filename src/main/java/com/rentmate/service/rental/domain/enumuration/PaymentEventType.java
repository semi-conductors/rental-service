package com.rentmate.service.rental.domain.enumuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentEventType {
   PAYMENT_PAID( "payment.paid"),
    PAYMENT_FAILED("payment.failed"),
    REFUNDED("payment.refunded"),
    FAILED_REFUND("payment.refund.failed");

  @JsonValue
  private final String value;
  @JsonCreator
  public static PaymentEventType fromValue(String value){
      for (PaymentEventType type:values()){
          if (type.getValue().equalsIgnoreCase(value)){
              return type;
          }
      }
      throw new IllegalArgumentException("Unknown payment event type: " + value);
  }
}
