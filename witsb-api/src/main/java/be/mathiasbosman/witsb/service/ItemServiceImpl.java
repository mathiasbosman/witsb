package be.mathiasbosman.witsb.service;

import be.mathiasbosman.fs.service.FileService;
import be.mathiasbosman.witsb.db.ItemRepository;
import be.mathiasbosman.witsb.db.WitsbRepository;
import be.mathiasbosman.witsb.entity.Item;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final ItemRepository itemRepository;
  private final FileService fileService;

  @Override
  public Item create(String filename, InputStream content) {
    log.debug("Saving file with filename: {}", filename);
    Item item = itemRepository
        .save(new Item(filename, LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())));
    fileService.save(content, item.getId().toString());
    if (!fileService.exists(item.getId().toString())) {
      throw new IllegalStateException("Item " + item + " was not saved to the file system");
    }
    return item;
  }

  @Override
  public Item delete(UUID id) {
    log.debug("Deleting file with id: {}", id);
    Item item = WitsbRepository.getById(itemRepository, id);
    fileService.delete(item.getId().toString());
    if (fileService.exists(item.getId().toString())) {
      throw new IllegalStateException("Item " + item + " still present on the file system");
    }
    itemRepository.delete(WitsbRepository.getById(itemRepository, id));
    return item;
  }

  @Override
  public InputStream load(UUID id) {
    log.debug("Loading item with id: {}", id);
    Item item = WitsbRepository.getById(itemRepository, id);
    if (!fileService.exists(item.getId().toString())) {
      throw new IllegalStateException("Item " + item + " could not be found on the file system");
    }
    return fileService.open(item.getId().toString());
  }

  @Override
  public List<Item> autoDelete(int amount, TemporalUnit temporalUnit) {
    Instant threshold = Instant.now().minus(amount, temporalUnit);
    LocalDateTime uploadedBeforeDate = LocalDateTime.ofInstant(threshold, ZoneId.systemDefault());
    log.info("Auto deleting items older than {} {} (uploaded before {})",
        amount, temporalUnit.toString().toLowerCase(), uploadedBeforeDate);
    List<Item> allByUploadedBefore = itemRepository
        .findAllByUploadedBefore(uploadedBeforeDate);
    List<Item> deletedItems = allByUploadedBefore.stream()
        .map(item -> delete(item.getId()))
        .collect(Collectors.toList());
    log.info("Deleted {} files", deletedItems.size());
    return deletedItems;
  }
}
