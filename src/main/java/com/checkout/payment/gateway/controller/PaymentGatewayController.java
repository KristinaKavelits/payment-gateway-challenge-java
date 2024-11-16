package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("api")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @Operation(summary = "Get payment by ID", description = "Retrieves the details of a payment using its unique identifier.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Payment found"),
      @ApiResponse(responseCode = "404", description = "Payment not found")
  })
  @GetMapping("/payment/{id}")
  public ResponseEntity<PostPaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  @Operation(summary = "Submit a payment", description = "Submits a payment request, validates payment details, and returns the payment status (authorized, declined, or rejected)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "202", description = "Payment has been authorized"),
      @ApiResponse(responseCode = "400", description = "Invalid request. The payment was rejected due to bad input (e.g., invalid card details)"),
      @ApiResponse(responseCode = "422", description = "Payment declined by the acquiring bank.")
  })
  @PostMapping("/payment/submit")
  public ResponseEntity<PostPaymentResponse> postPaymentEvent(@Valid @RequestBody PostPaymentRequest paymentRequest) {
    PostPaymentResponse paymentResponse = paymentGatewayService.processPayment(paymentRequest);
    HttpStatus httpStatus = mapPaymentStatusToHttpStatus(paymentResponse.getStatus());
    return new ResponseEntity<>(paymentResponse, httpStatus);
  }

  /**
   * Maps the status of the payment response to the corresponding HttpStatus.
   *
   * @param paymentStatus the status of the payment response
   * @return the appropriate HTTP status code
   */
  private HttpStatus mapPaymentStatusToHttpStatus(PaymentStatus paymentStatus) {
    return switch (paymentStatus) {
      case AUTHORIZED -> HttpStatus.ACCEPTED;
      case REJECTED -> HttpStatus.BAD_REQUEST;
      case DECLINED -> HttpStatus.UNPROCESSABLE_ENTITY;
    };
  }
}
