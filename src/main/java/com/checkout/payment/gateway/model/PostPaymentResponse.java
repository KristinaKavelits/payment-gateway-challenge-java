package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public class PostPaymentResponse {

  @JsonProperty("id")
  @Schema(description = "Unique identifier for the processed payment.", example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @JsonProperty("status")
  @Schema(description = "Status of the payment (e.g., Authorized, Declined).", example = "Authorized")
  private PaymentStatus status;

  @JsonProperty("cardNumberLastFour")
  @Schema(description = "The last four digits of the card number used.", example = "1234")
  private String cardNumberLastFour;

  @JsonProperty("expiryDate")
  @Schema(description = "The expiration date of the card in MM/YYYY format.", example = "12/2025")
  private String expiryDate;

  @JsonProperty("currency")
  @Schema(description = "The 3-character ISO currency code (e.g., USD).", example = "USD")
  private String currency;

  @JsonProperty("amount")
  @Schema(description = "The amount that was processed.", example = "1050")
  private int amount;


  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public String getCardNumberLastFour() {
    return cardNumberLastFour;
  }

  public void setCardNumberLastFour(String cardNumberLastFour) {
    this.cardNumberLastFour = cardNumberLastFour;
  }

  public String getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(String expiryDate) {
    this.expiryDate = expiryDate;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  @Override
  public String toString() {
    return "GetPaymentResponse{" +
        "id=" + id +
        ", status=" + status +
        ", cardNumberLastFour=" + cardNumberLastFour +
        ", expiryDate=" + expiryDate +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        '}';
  }
}
