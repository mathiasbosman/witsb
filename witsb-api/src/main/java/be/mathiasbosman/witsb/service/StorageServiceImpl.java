package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.db.WitsbRepository;
import be.mathiasbosman.witsb.entity.Item;
import be.mathiasbosman.fs.domain.FileNode;
import be.mathiasbosman.fs.service.FileService;
import java.io.IOException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class StorageServiceImpl implements StorageService {
  private final ItemService itemService;
  private final FileService fileService;

  @Override
  public void upload(MultipartFile file) {
    Item item = itemService.create(file.getOriginalFilename());
    try {
      fileService.save(file.getInputStream(), item.getId().toString());
    } catch (IOException e) {
      itemService.delete(item.getId());
      throw new IllegalStateException("Could not save file " + file.getOriginalFilename(), e);
    }
  }

  @Override
  public Resource getResource(UUID id) {
    FileNode fileNode = fileService.get(id.toString());
    return new InputStreamResource(fileService.open(fileNode));
  }

}
