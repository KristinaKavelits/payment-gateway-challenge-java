package com.checkout.payment.gateway.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import javax.money.Monetary;
import javax.money.UnknownCurrencyException;
import java.util.Arrays;
import java.util.List;


public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

  private static final List<String> ALLOWED_CURRENCY_CODES = Arrays.asList("USD", "EUR", "GBP");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return false;
    }

    if (ALLOWED_CURRENCY_CODES.contains(value)) {
      try {
        Monetary.getCurrency(value);
        return true;
      } catch (UnknownCurrencyException e) {
        return false;
      }
    }

    return false;
  }
}
