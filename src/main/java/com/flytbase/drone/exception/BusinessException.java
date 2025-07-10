package com.flytbase.drone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception for business logic errors. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

  /**
   * Create a new business exception with the specified message.
   *
   * @param message the error message
   */
  public BusinessException(String message) {
    super(message);
  }

  /**
   * Create a new business exception with the specified message and cause.
   *
   * @param message the error message
   * @param cause the cause of the exception
   */
  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }
}
