package be.mathiasbosman.witsb.exception;

public class WitsbException extends RuntimeException {

  /**
   * Constructor with message. Simply overrides the parent constructor.
   *
   * @param message the message
   * @see RuntimeException
   */
  public WitsbException(String message) {
    super(message);
  }
}
