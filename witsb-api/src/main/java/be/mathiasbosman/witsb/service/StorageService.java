package be.mathiasbosman.witsb.service;

import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
  void upload(MultipartFile file);

  Resource getResource(UUID id);
}
