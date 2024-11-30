package be.mathiasbosman.witsb.controller;

import be.mathiasbosman.witsb.exception.EmptyFileException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice {

  @ExceptionHandler(EmptyFileException.class)
  public ResponseEntity<EmptyFileException> handleEmptyFileException(EmptyFileException ex) {
    return ResponseEntity.badRequest().body(ex);
  }
}