package be.mathiasbosman.witsb;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
public class WitsbPostgresqlContainer extends PostgreSQLContainer<WitsbPostgresqlContainer> {

  private static final String IMAGE_VERSION = "postgres:14.5";
  private static WitsbPostgresqlContainer container;

  private WitsbPostgresqlContainer() {
    super(IMAGE_VERSION);
  }

  public static WitsbPostgresqlContainer getInstance() {
    if (container == null) {
      container = new WitsbPostgresqlContainer();
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    System.setProperty("DB_URL", container.getJdbcUrl());
    System.setProperty("DB_USERNAME", container.getUsername());
    System.setProperty("DB_PASSWORD", container.getPassword());
    log.info("Container started on {}", container.getJdbcUrl());
  }

  @Override
  public void stop() {
    log.info("Container stopped");
  }
}
