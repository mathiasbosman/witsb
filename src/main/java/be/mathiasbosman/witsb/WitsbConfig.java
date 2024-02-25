package be.mathiasbosman.witsb;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.fs.service.nio.NioFileService;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class WitsbConfig {

  @Bean
  @Profile("local")
  public FileService testFileService() {
    final FileSystem fileSystem = NioFileService.DEFAULT_FILE_SYSTEM;
    final Path tempDir = fileSystem.getPath("target/" + UUID.randomUUID() + "/");
    log.info("Creating local directory {}", tempDir);
    return new NioFileService(fileSystem, tempDir.toString());
  }
}
