package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.entity.Item;
import java.util.UUID;

public interface ItemService {

  Item create(String filename);

  void delete(UUID id);
}
