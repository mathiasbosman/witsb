package be.mathiasbosman.witsb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(ApplicationConfig.PREFIX)
public class ApplicationConfig {

  public static final String PREFIX = "witsb";

  private FileServiceConfig fs;

  @Getter
  @Setter
  public static class FileServiceConfig {

    private String minioUrl;
    private String minioKey;
    private String minioSecret;
    private String minioBucket;
  }
}
