package com.michael.poc.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private final int status;
  private final String message;
  private final LocalDateTime timestamp;

  private final List<String> errors;

  public ErrorResponse(int status, String message) {
    this.status = status;
    this.message = message;
    this.timestamp = LocalDateTime.now();
    this.errors = null;
  }

  public ErrorResponse(int status, String message, List<String> errors) {
    this.status = status;
    this.message = message;
    this.timestamp = LocalDateTime.now();
    this.errors = errors;
  }
}
