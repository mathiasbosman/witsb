package be.mathiasbosman.witsb;

import be.mathiasbosman.fs.service.FileService;
import be.mathiasbosman.fs.service.aws.s3.AmazonS3Factory;
import be.mathiasbosman.fs.service.aws.s3.S3FileSystem;
import be.mathiasbosman.witsb.ApplicationConfig.FileServiceConfig;
import com.amazonaws.services.s3.AmazonS3;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class S3Config {

  private final ApplicationConfig applicationConfig;

  @Bean
  public FileService fileService() {
    FileServiceConfig fsConfig = applicationConfig.getFs();
    AmazonS3 s3 = AmazonS3Factory.builder()
        .key(fsConfig.getMinioKey())
        .secret(fsConfig.getMinioSecret())
        .bucket(fsConfig.getMinioBucket())
        .serviceEndpoint(fsConfig.getMinioUrl())
        .pathStyleAccessEnabled(true)
        .createBucketIfMissing(true)
        .build()
        .toAmazonS3();
    return new S3FileSystem(s3, fsConfig.getMinioBucket());

  }
}
