package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.validation.ValidCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;

@Schema(description = "Request object containing payment details for processing.")
@Data
public class PostPaymentRequest implements Serializable {

  @JsonProperty("card_number")
  @Schema(description = "The card number for the payment.", example = "1111111111111111")
  @NotNull(message = "Card number is required")
  @Pattern(regexp = "\\d+", message = "Card number must contain only numeric characters")
  @Size(min = 14, max = 19, message = "Card number must be between 14 and 19 characters")
  private String cardNumber;

  @JsonProperty("expiry_month")
  @Schema(description = "The expiration month of the card (1-12).", example = "12")
  @NotNull(message = "Expiry month is required")
  @Pattern(regexp = "(0[1-9]|1[0-2])", message = "Expiry month must be a valid 2-digit month (01-12)")
  private String expiryMonth;

  @JsonProperty("expiry_year")
  @Schema(description = "The expiration year of the card. Month+year combination should be not expired", example = "2025")
  @NotNull(message = "Expiry year is required")
  @Pattern(regexp = "\\d{4}", message = "Expiry year must be a 4-digit number")
  private String expiryYear;

  @JsonProperty("currency")
  @Schema(description = "The 3-character ISO currency code (e.g., USD).", example = "USD")
  @NotNull(message = "Currency is required")
  @Size(min = 3, max = 3, message = "Currency must be a 3-character ISO code")
  @ValidCurrency(message = "Invalid ISO currency code")
  private String currency;

  @JsonProperty("amount")
  @Schema(description = "The amount for the payment, represented in the smallest currency unit (e.g., cents).", example = "1050")
  @NotNull(message = "Amount is required")
  @Min(value = 1, message = "Amount should be more than 0")
  private int amount;

  @JsonProperty("cvv")
  @Schema(description = "The card's CVV (Card Verification Value), 3 or 4 digits.", example = "123")
  @NotNull(message = "CVV is required")
  @Size(min = 3, max = 4, message = "CVV must be between 3 and 4 digits")
  @Pattern(regexp = "^[0-9]+$", message = "CVV must only contain numeric characters")
  private String cvv;

  @Schema(hidden = true)
  public String getCardNumberLastFourDigits() {
    return showLastFourSymbolsOfCardNumber();
  }

  @Schema(hidden = true)
  public String getExpiryDate() {
    String formattedMonth = expiryMonth.length() == 1 ? "0" + expiryMonth : expiryMonth;
    return String.format("%s/%s", formattedMonth, expiryYear);
  }

  private String showLastFourSymbolsOfCardNumber() {
    return cardNumber.substring(cardNumber.length() - 4);
  }

}
