package com.orbit.licensehub.controller;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
  public Map<String, Object> handleBadRequest(Exception ex) {
    if (ex instanceof MethodArgumentNotValidException validationEx) {
      Map<String, String> fields =
          validationEx.getBindingResult().getFieldErrors().stream()
              .collect(
                  java.util.stream.Collectors.toMap(
                      FieldError::getField,
                      FieldError::getDefaultMessage,
                      (a, b) -> a));
      return Map.of("error", "VALIDATION_ERROR", "fields", fields);
    }

    return Map.of("error", ex.getMessage());
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(EntityNotFoundException.class)
  public Map<String, Object> handleNotFound(EntityNotFoundException ex) {
    return Map.of("error", ex.getMessage());
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(AccessDeniedException.class)
  public Map<String, Object> handleDenied(AccessDeniedException ex) {
    return Map.of("error", ex.getMessage());
  }
}
