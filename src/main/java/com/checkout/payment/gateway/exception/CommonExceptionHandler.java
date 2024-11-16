package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.UUID;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(PaymentProcessingException.class)
  public ResponseEntity<ErrorResponse> handleEventProcessingException(PaymentProcessingException ex) {
    LOG.error("Exception happened while processing the payment with bank {}", ex.getMessage(), ex);
    return new ResponseEntity<>(new ErrorResponse(PaymentStatus.REJECTED.getName(), ex.getMessage(),
        UUID.randomUUID().toString()), HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
    LOG.error("Requested resource not found. Details: {}", ex.getMessage(), ex);
    return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(),
        UUID.randomUUID().toString()),
        HttpStatus.NOT_FOUND);
  }
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidExceptions(MethodArgumentNotValidException ex) {
    StringBuilder errorMessage = new StringBuilder();
    ex.getBindingResult().getFieldErrors().forEach(error -> {
      errorMessage.append(error.getDefaultMessage())
          .append(".");
    });
    LOG.error("Validation failed for request data. Error(s): {}", errorMessage);

    ErrorResponse errorResponse = new ErrorResponse(
        PaymentStatus.REJECTED.getName(),
        errorMessage.toString(),
        UUID.randomUUID().toString()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidExpiryDateException.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(InvalidExpiryDateException ex) {
    LOG.error("Expiry date validation failed: {}", ex.getMessage(), ex);
    return new ResponseEntity<>(new ErrorResponse(PaymentStatus.REJECTED.getName(),
        ex.getMessage(), UUID.randomUUID().toString()), HttpStatus.BAD_REQUEST);
  }
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
    LOG.error("Unexpected error occurred {}", ex.getMessage(), ex);
    return new ResponseEntity<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        ex.getMessage(), UUID.randomUUID().toString()), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
