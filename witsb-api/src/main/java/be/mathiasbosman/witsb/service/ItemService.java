package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.entity.Item;
import java.io.InputStream;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.UUID;

public interface ItemService {

  Item create(String filename, InputStream content);

  Item delete(UUID id);

  InputStream load(UUID id);

  List<Item> autoDelete(int amount, TemporalUnit temporalUnit);
}
