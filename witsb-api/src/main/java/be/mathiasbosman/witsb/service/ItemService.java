package be.mathiasbosman.witsb.service;

import be.mathiasbosman.db.ItemRepository;
import be.mathiasbosman.entity.Item;
import be.mathiasbosman.fs.service.FileService;
import java.io.InputStream;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;
  private final FileService fileService;

  public Item save(InputStream stream, String fileName) {
    Item item = itemRepository.save(new Item(fileName));
    String targetFileName = item.getId() + "_" + fileName;
    fileService.save(stream, targetFileName);
    return item;
  }
}
