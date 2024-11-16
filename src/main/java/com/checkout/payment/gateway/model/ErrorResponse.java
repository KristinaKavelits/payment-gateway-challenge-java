package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorResponse {
  private String status;
  private String message;
  private String traceId;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
  private LocalDateTime timestamp;

  private ErrorResponse() {
    timestamp = LocalDateTime.now();
  }
  public ErrorResponse(String status, String message, String traceId) {
    this();
    this.status = status;
    this.message = message;
    this.traceId = traceId;
  }
  public String getTraceId() {
    return traceId;
  }

  public String getMessage() {
    return message;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public String getStatus() {
    return status;
  }
  @Override
  public String toString() {
    return "ErrorResponse{" +
        "status='" + status + '\'' +
        ", message='" + message + '\'' +
        ", traceID='" + traceId + '\'' +
        ", timestamp='" + timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss")) + '\'' +
        '}';
  }
}
