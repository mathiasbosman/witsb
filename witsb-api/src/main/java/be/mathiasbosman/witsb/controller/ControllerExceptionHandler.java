package be.mathiasbosman.witsb.controller;

import be.mathiasbosman.witsb.dto.WitsbError;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
public class ControllerExceptionHandler {

  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(value = {RuntimeException.class})
  public WitsbError handle(RuntimeException ex) {
    return new WitsbError(ExceptionUtils.getRootCause(ex));
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  @ExceptionHandler(MultipartException.class)
  public ResponseEntity<String> handleMaxUploadSizeExceededException(
      final MultipartException exception) {
    SizeLimitExceededException ex = (SizeLimitExceededException) ExceptionUtils
        .getRootCause(exception);
    String message =
        "The file is too large (" + FileUtils.byteCountToDisplaySize(ex.getActualSize())
            + "). The maximum allowed size is " + FileUtils
            .byteCountToDisplaySize(ex.getPermittedSize()) + ".";
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(message);
  }
}
