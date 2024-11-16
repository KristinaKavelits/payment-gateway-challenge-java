package com.checkout.payment.gateway.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentProcessingException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import java.util.UUID;
import java.util.stream.Stream;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class PaymentGatewayControllerPostTest {

  @MockBean
  private PaymentGatewayService paymentGatewayService;
  @Autowired
  private MockMvc mvc;
  @Autowired
  private ObjectMapper objectMapper;

  @ParameterizedTest
  @MethodSource("validPaymentRequestAndAuthorizedResponse")
  void whenRequestIsValidTheResponseIsCorrect(PostPaymentRequest paymentRequest,
      PostPaymentResponse paymentResponse)
      throws Exception {

    when(paymentGatewayService.processPayment(any(PostPaymentRequest.class))).thenReturn(
        paymentResponse);

    mvc.perform(MockMvcRequestBuilders.post("/payment/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.status").value(paymentResponse.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(paymentResponse.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryDate").value(paymentResponse.getExpiryDate()))
        .andExpect(jsonPath("$.currency").value(paymentResponse.getCurrency()))
        .andExpect(jsonPath("$.amount").value(paymentResponse.getAmount()));
  }

  @ParameterizedTest
  @MethodSource("invalidPaymentRequestAndResponse")
  void whenRequestIsInvalidTheResponseIsCorrect(PostPaymentRequest paymentRequest,
      PostPaymentResponse paymentResponse)
      throws Exception {

    when(paymentGatewayService.processPayment(any(PostPaymentRequest.class))).thenReturn(
        paymentResponse);

    mvc.perform(MockMvcRequestBuilders.post("/payment/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(paymentResponse.getStatus().getName()))
        .andExpect(jsonPath("$.traceId").isNotEmpty())
        .andExpect(
            jsonPath("$.message").value("Expiry month must be a valid 2-digit month (01-12)."));
  }

  @ParameterizedTest
  @MethodSource("validPaymentRequestAndUnauthorizedResponse")
  void whenRequestIsValidButTheResponseIsDeclined(PostPaymentRequest paymentRequest,
      PostPaymentResponse paymentResponse)
      throws Exception {

    when(paymentGatewayService.processPayment(any(PostPaymentRequest.class))).thenReturn(
        paymentResponse);

    mvc.perform(MockMvcRequestBuilders.post("/payment/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.status").value(paymentResponse.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(paymentResponse.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryDate").value(paymentResponse.getExpiryDate()))
        .andExpect(jsonPath("$.currency").value(paymentResponse.getCurrency()))
        .andExpect(jsonPath("$.amount").value(paymentResponse.getAmount()));
  }

  @ParameterizedTest
  @MethodSource("validPaymentRequestAndAuthorizedResponse")
  void shouldReturnBadGatewayInTheMessageWhenResponseFromBankFailed(PostPaymentRequest paymentRequest)
      throws Exception {

    when(paymentGatewayService.processPayment(any(PostPaymentRequest.class))).thenThrow(
        PaymentProcessingException.class);

    mvc.perform(MockMvcRequestBuilders.post("/payment/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()))
        .andExpect(jsonPath("$.traceId").isNotEmpty());
  }

  @Test
  void shouldReturnBadRequestWhenCurrencyInvalid() throws Exception {
    PostPaymentRequest paymentRequest = generatePaymentRequest("112234564367325", "01",
        "2028", "NOT", 1234, "055");

    mvc.perform(MockMvcRequestBuilders.post("/payment/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.traceId").isNotEmpty())
        .andExpect(jsonPath("$.status").value("Rejected"))
        .andExpect(jsonPath("$.message").value("Invalid ISO currency code."));
  }

  @Test
  void shouldReturnBadRequestWhenCvvIncorrect() throws Exception {
    PostPaymentRequest paymentRequest = generatePaymentRequest("112234564367325", "01",
        "2028", "GBP", 1234567890, "05576");

    mvc.perform(MockMvcRequestBuilders.post("/payment/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.traceId").isNotEmpty())
        .andExpect(jsonPath("$.status").value("Rejected"))
        .andExpect(jsonPath("$.message").value("CVV must be between 3 and 4 digits."));
  }

  private static Stream<Arguments> validPaymentRequestAndAuthorizedResponse() {
    return Stream.of(
        Arguments.of(
            generatePaymentRequest("4111111111111234", "12", "2026", "USD", 1000, "123"),
            generatePaymentResponse(UUID.randomUUID(), "1234", PaymentStatus.AUTHORIZED, "12/2026",
                "USD",
                1000))
    );
  }

  private static Stream<Arguments> validPaymentRequestAndUnauthorizedResponse() {
    return Stream.of(
        Arguments.of(
            generatePaymentRequest("5417611333117865", "02", "2027", "GBP", 1, "622"),
            generatePaymentResponse(UUID.randomUUID(), "7865", PaymentStatus.DECLINED, "02/2027",
                "GBP",
                1))
    );
  }

  private static Stream<Arguments> invalidPaymentRequestAndResponse() {
    return Stream.of(
        Arguments.of(
            generatePaymentRequest("5123456741114321", "13", "2028", "USD", 1000, "123"),
            generatePaymentResponse(null, "", PaymentStatus.REJECTED, "", "",
                1000))
    );
  }

  private static PostPaymentRequest generatePaymentRequest(String cardNr, String expiryMonth,
      String expiryYear, String currency, int amount, String cvv) {

    PostPaymentRequest paymentRequest = new PostPaymentRequest();
    paymentRequest.setCardNumber(cardNr);
    paymentRequest.setExpiryMonth(expiryMonth);
    paymentRequest.setExpiryYear(expiryYear);
    paymentRequest.setCurrency(currency);
    paymentRequest.setAmount(amount);
    paymentRequest.setCvv(cvv);
    return paymentRequest;
  }

  private static PostPaymentResponse generatePaymentResponse(UUID uuid, String cardNrLastFour,
      PaymentStatus paymentStatus,
      String expiryDate, String currency, int amount) {

    PostPaymentResponse paymentResponse = new PostPaymentResponse();
    paymentResponse.setId(uuid);
    paymentResponse.setCardNumberLastFour(cardNrLastFour);
    paymentResponse.setExpiryDate(expiryDate);
    paymentResponse.setCurrency(currency);
    paymentResponse.setAmount(amount);
    paymentResponse.setStatus(paymentStatus);
    return paymentResponse;
  }
}

