package be.mathiasbosman.witsb.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import be.mathiasbosman.witsb.exception.EmptyFileException;
import be.mathiasbosman.witsb.exception.WitsbException;
import org.junit.jupiter.api.Test;
import org.springframework.web.ErrorResponse;

class ExceptionControllerAdviceTest {

  private final ExceptionControllerAdvice exceptionControllerAdvice = new ExceptionControllerAdvice();

  @Test
  void handleEmptyFileException() {
    EmptyFileException ex = new EmptyFileException("File is empty");
    ErrorResponse response = exceptionControllerAdvice.handleEmptyFileException(ex);

    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertThat(response.getBody()).isNotNull().satisfies(problemDetail -> {
      assertThat(problemDetail.getDetail()).isEqualTo("File is empty");
      assertThat(problemDetail.getTitle()).isEqualTo("Empty file");
      assertThat(problemDetail.getProperties()).containsKey("timestamp");
    });
  }

  @Test
  void handleWitsbException() {
    WitsbException ex = new WitsbException("Internal foo error");
    ErrorResponse response = exceptionControllerAdvice.handleWitsbException(ex);

    assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull().satisfies(problemDetail -> {
      assertThat(problemDetail.getDetail()).isEqualTo("Internal foo error");
      assertThat(problemDetail.getTitle()).isEqualTo("Internal server error");
      assertThat(problemDetail.getProperties()).containsKey("timestamp");
    });
  }
}