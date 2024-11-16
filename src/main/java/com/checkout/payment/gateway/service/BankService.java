package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.exception.PaymentProcessingException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;

@Service
public class BankService {

  private static final Logger LOG = LoggerFactory.getLogger(BankService.class);
  private final RestTemplate restTemplate;
  private final String bankApiUrl = "http://localhost:8080/payments";

  public BankService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  public BankPaymentResponse processPaymentWithBank(BankPaymentRequest paymentRequestToBank, UUID uuid) {
    try {
      LOG.info("Sending payment request to bank: ID: {}, Amount: {}, Currency: {}",
          uuid , paymentRequestToBank.getAmount(), paymentRequestToBank.getCurrency());
      ResponseEntity<BankPaymentResponse> bankPaymentResponse = restTemplate.postForEntity(
          bankApiUrl, paymentRequestToBank, BankPaymentResponse.class
      );
      LOG.info("Received response from bank: Status Code: {}", bankPaymentResponse.getStatusCode());
      handleResponseErrors(bankPaymentResponse);
      return bankPaymentResponse.getBody();
    } catch (RestClientException ex) {
      LOG.error("Failed to communicate with the bank. Error: {}", ex.getMessage(), ex);
      throw new PaymentProcessingException("We couldn't reach the bank to process your payment. Please check your connection or try again later.");
    }
  }

  private void handleResponseErrors(ResponseEntity<BankPaymentResponse> response) {
    if (response.getStatusCode().is4xxClientError()) {
      LOG.error("Client error occurred: Status Code: {}, Response: {}", response.getStatusCode(), response.getBody());
      throw new PaymentProcessingException("There was an issue with your payment details. Please review and try again.");
    } else if (response.getStatusCode().is5xxServerError()) {
      LOG.error("Server error at the bank's side. Status Code: {}, Response: {}", response.getStatusCode(), response.getBody());
      throw new PaymentProcessingException("We’re currently experiencing issues processing your payment. Please try again later.");
    }
  }

}
