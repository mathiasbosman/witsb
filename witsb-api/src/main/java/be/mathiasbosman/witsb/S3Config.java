package be.mathiasbosman.witsb;

import be.mathiasbosman.fs.service.FileService;
import be.mathiasbosman.fs.service.aws.s3.S3FileSystem;
import be.mathiasbosman.witsb.ApplicationConfig.FileServiceConfig;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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
    return new S3FileSystem(AmazonS3ClientBuilder
        .standard()
        .withCredentials(new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(fsConfig.getMinioKey(), fsConfig.getMinioSecret())))
        .withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration(fsConfig.getMinioUrl(), null))
        .withPathStyleAccessEnabled(true)
        .build(), fsConfig.getMinioBucket());
  }
}
