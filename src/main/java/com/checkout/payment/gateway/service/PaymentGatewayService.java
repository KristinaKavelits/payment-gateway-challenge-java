package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.InvalidExpiryDateException;
import com.checkout.payment.gateway.exception.ResourceNotFoundException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.time.YearMonth;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final BankService bankService;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankService bankService) {
    this.paymentsRepository = paymentsRepository;
    this.bankService = bankService;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to the payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() ->
        new ResourceNotFoundException("Payment not found with ID: " + id)
    );
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    validateExpiryDate(paymentRequest);
    UUID uuid = UUID.randomUUID();
    LOG.debug("Processing payment request with UUID: {}", uuid);
    return processPaymentWithBank(paymentRequest, uuid);
  }

  private PostPaymentResponse processPaymentWithBank(PostPaymentRequest paymentRequest, UUID uuid) {
    BankPaymentRequest bankPaymentRequest = mapToBankPaymentRequest(paymentRequest);
    BankPaymentResponse bankPaymentResponse = bankService.processPaymentWithBank(bankPaymentRequest, uuid);
    PaymentStatus paymentStatus = bankPaymentResponse.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;
    if (paymentStatus.equals(PaymentStatus.AUTHORIZED)) {
      LOG.info("Payment authorized for UUID: {}, Amount: {}, Currency: {}",
          uuid, paymentRequest.getAmount(), paymentRequest.getCurrency());
    } else {
      LOG.warn("Payment declined for UUID: {}, Amount: {}, Currency: {}",
          uuid, paymentRequest.getAmount(), paymentRequest.getCurrency());
    }
    return mapToPaymentResponse(paymentRequest, paymentStatus, uuid);
  }

  private void validateExpiryDate(PostPaymentRequest paymentRequest) {
    if (!checkExpiryDateIsValid(paymentRequest.getExpiryYear(), paymentRequest.getExpiryMonth())) {
      throw new InvalidExpiryDateException("Invalid Expiration Date");
    }
  }

  private BankPaymentRequest mapToBankPaymentRequest(PostPaymentRequest paymentRequest) {
    BankPaymentRequest bankPaymentRequest = new BankPaymentRequest();
    bankPaymentRequest.setCardNumber(paymentRequest.getCardNumber());
    bankPaymentRequest.setExpiryDate(paymentRequest.getExpiryDate());
    bankPaymentRequest.setCurrency(paymentRequest.getCurrency());
    bankPaymentRequest.setAmount(paymentRequest.getAmount());
    bankPaymentRequest.setCvv(paymentRequest.getCvv());
    return bankPaymentRequest;
  }


  private PostPaymentResponse mapToPaymentResponse(PostPaymentRequest paymentRequest,
      PaymentStatus paymentStatus, UUID uuid) {
    PostPaymentResponse paymentResponse = new PostPaymentResponse();
    paymentResponse.setAmount(paymentRequest.getAmount());
    paymentResponse.setCurrency(paymentRequest.getCurrency());
    paymentResponse.setExpiryDate(paymentRequest.getExpiryDate());
    paymentResponse.setCardNumberLastFour(paymentRequest.getCardNumberLastFourDigits());
    paymentResponse.setStatus(paymentStatus);
    paymentResponse.setId(uuid);
    paymentsRepository.add(paymentResponse);
    LOG.debug("Payment saved to repository. UUID: {}, Amount: {}, Status: {}",
        paymentResponse.getId(), paymentResponse.getAmount(), paymentResponse.getStatus());
    return paymentResponse;

  }
  private boolean checkExpiryDateIsValid(String expiryYear, String expiryMonth) {
    int requestYear = Integer.parseInt(expiryYear);
    int requestMonth = Integer.parseInt(expiryMonth);
    YearMonth currentYearMonth = YearMonth.now();
    YearMonth expiryYearMonth = YearMonth.of(requestYear, requestMonth);
    return expiryYearMonth.isAfter(currentYearMonth);
  }

}
