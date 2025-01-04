package be.mathiasbosman.witsb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class to start the application.
 */
@SpringBootApplication
public class WitsbApplication {

  /**
   * Main method to start the application.
   *
   * @param args the Java arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(WitsbApplication.class, args);
  }

}
