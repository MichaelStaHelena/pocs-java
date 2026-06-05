package com.michael.poc.exception;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleValidationError(MethodArgumentNotValidException ex) {

    List<String> errors =
        ex.getBindingResult().getAllErrors().stream()
            .map(
                error -> {
                  String field = ((FieldError) error).getField();
                  return field + ": " + error.getDefaultMessage();
                })
            .toList();

    log.warn("validation failed: {}", errors);
    return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNotFound(NoSuchElementException ex) {
    log.warn("resource not found: {}", ex.getMessage());
    return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleGenericError(Exception ex) {
    log.error("unexpected error", ex);
    return new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred");
  }
}
