package be.mathiasbosman.witsb;

import static java.nio.file.Files.createDirectories;

import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Transactional
@Testcontainers
public abstract class ContainerTest {

  @Container
  @SuppressWarnings("unused")
  public static PostgreSQLContainer<WitsbPostgresqlContainer> postgreSQLContainer = WitsbPostgresqlContainer.getInstance();

  @BeforeEach
  void setup() throws IOException {
    createDirectories(TestConfig.tempDir);
  }

  @AfterEach
  void cleanup() throws IOException {
    FileUtils.deleteDirectory(TestConfig.tempDir.toFile());
  }
}
