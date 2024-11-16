package com.checkout.payment.gateway.service;


import com.checkout.payment.gateway.exception.InvalidExpiryDateException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PaymentGatewayServiceTest {
  @InjectMocks
  private PaymentGatewayService paymentGatewayService;

  private PostPaymentRequest paymentRequest;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    paymentRequest = new PostPaymentRequest();
    paymentRequest.setCardNumber("4111111111113256");
    paymentRequest.setExpiryMonth("12");
    paymentRequest.setExpiryYear("2026");
    paymentRequest.setCurrency("USD");
    paymentRequest.setAmount(11);
    paymentRequest.setCvv("079");
  }

  @Test
  void testProcessPaymentThrowsExceptionWhenInvalidExpiryDate() {
    paymentRequest.setExpiryYear("2020");
    assertThrows(InvalidExpiryDateException.class, () -> paymentGatewayService.processPayment(paymentRequest));
  }

}
