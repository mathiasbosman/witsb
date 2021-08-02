package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.db.ItemRepository;
import be.mathiasbosman.witsb.db.WitsbRepository;
import be.mathiasbosman.witsb.entity.Item;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final ItemRepository itemRepository;

  @Override
  public Item create(String filename) {
    return itemRepository.save(new Item(filename));
  }

  @Override
  public void delete(UUID id) {
    itemRepository.delete(WitsbRepository.getById(itemRepository, id));
  }
}
