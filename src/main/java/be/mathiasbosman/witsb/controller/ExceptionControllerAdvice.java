package be.mathiasbosman.witsb.controller;

import be.mathiasbosman.witsb.exception.EmptyFileException;
import be.mathiasbosman.witsb.exception.WitsbException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

  @ExceptionHandler(EmptyFileException.class)
  ErrorResponse handleEmptyFileException(EmptyFileException ex) {
    return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
        .title("Empty file")
        .property("timestamp", Instant.now())
        .build();
  }

  @ExceptionHandler(WitsbException.class)
  ErrorResponse handleWitsbException(WitsbException ex) {
    return ErrorResponse.builder(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage())
        .title("Internal server error")
        .property("timestamp", Instant.now())
        .build();
  }
}