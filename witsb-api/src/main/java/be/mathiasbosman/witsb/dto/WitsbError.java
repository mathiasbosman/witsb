package be.mathiasbosman.witsb.dto;

public class WitsbError {

  private final String message;

  public WitsbError(Throwable e) {
    this(e.getMessage());
  }

  public WitsbError(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
