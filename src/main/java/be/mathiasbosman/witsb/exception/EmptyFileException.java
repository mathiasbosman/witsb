package be.mathiasbosman.witsb.exception;

public class EmptyFileException extends RuntimeException {

  /**
   * Constructor with message. Simply overrides the parent constructor.
   *
   * @param message the message
   * @see RuntimeException
   */
  public EmptyFileException(String message) {
    super(message);
  }
}
