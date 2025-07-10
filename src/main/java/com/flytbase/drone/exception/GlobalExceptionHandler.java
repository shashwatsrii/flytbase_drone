package com.flytbase.drone.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Global exception handler for the application. */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handle validation exceptions.
   *
   * @param ex the exception
   * @return the response entity
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    ErrorResponse errorResponse =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            errors.toString(),
            LocalDateTime.now());

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle authentication exceptions.
   *
   * @param ex the exception
   * @return the response entity
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(BadCredentialsException ex) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Error",
            "Invalid email or password",
            LocalDateTime.now());

    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handle username not found exceptions.
   *
   * @param ex the exception
   * @return the response entity
   */
  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
      UsernameNotFoundException ex) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            HttpStatus.NOT_FOUND.value(), "User Error", ex.getMessage(), LocalDateTime.now());

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Handle business logic exceptions.
   *
   * @param ex the exception
   * @return the response entity
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Business Error", ex.getMessage(), LocalDateTime.now());

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle general exceptions.
   *
   * @param ex the exception
   * @return the response entity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralExceptions(Exception ex) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Server Error",
            ex.getMessage(),
            LocalDateTime.now());

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Error response DTO. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
  }
}
