package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BankPaymentRequest {

  @JsonProperty("card_number")
  private String cardNumber;

  @JsonProperty("expiry_date")
  private String expiryDate;

  @JsonProperty("currency")
  private String currency;

  @JsonProperty("amount")
  private int amount;

  @JsonProperty("cvv")
  private String cvv;

}
