package be.mathiasbosman.witsb;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.fs.service.nio.NioFileService;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TestConfig {

  private static final FileSystem fileSystem = NioFileService.DEFAULT_FILE_SYSTEM;
  public static final Path tempDir = fileSystem.getPath(
      "target/" + System.identityHashCode(TestConfig.class) + "/");

  @Bean
  public FileService testFileService() {
    log.info("Creating test directory {}", tempDir);
    return new NioFileService(fileSystem, tempDir.toString());
  }
}
