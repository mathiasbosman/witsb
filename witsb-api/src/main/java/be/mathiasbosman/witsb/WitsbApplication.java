package be.mathiasbosman.witsb;

import be.mathiasbosman.fs.service.FileService;
import be.mathiasbosman.fs.service.nio.NIOFileService;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WitsbApplication {

  public static void main(String[] args) {
    SpringApplication.run(WitsbApplication.class, args);
  }

  @Bean
  public FileService fileService() throws IOException {
    String tmpDir = Files.createTempDirectory("witsb").toFile().getAbsolutePath();
    return new NIOFileService(tmpDir);
  }
}
