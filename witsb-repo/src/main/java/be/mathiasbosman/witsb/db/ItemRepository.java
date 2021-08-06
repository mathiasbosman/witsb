package be.mathiasbosman.witsb.db;

import be.mathiasbosman.witsb.entity.Item;
import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends WitsbRepository<Item> {

  List<Item> findAllByUploadedBefore(LocalDateTime threshold);
}
