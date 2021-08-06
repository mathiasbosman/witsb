package be.mathiasbosman.witsb.dto;

import be.mathiasbosman.witsb.entity.Item;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemDto {

  private final UUID id;
  private final String name;
  private final LocalDateTime uploaded;

  public ItemDto(Item item) {
    this(item.getId(), item.getName(), item.getUploaded());
  }
}
