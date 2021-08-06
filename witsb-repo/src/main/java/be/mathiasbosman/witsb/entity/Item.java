package be.mathiasbosman.witsb.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@NoArgsConstructor
@Table(name = "items")
public class Item extends AbstractWitsbEntity {

  private String name;
  private LocalDateTime uploaded;

  @Builder
  public Item(String name, LocalDateTime uploaded) {
    this.name = name;
    this.uploaded = uploaded != null ? uploaded : LocalDateTime.now();
  }

  public String getName() {
    return name;
  }

  public LocalDateTime getUploaded() {
    return uploaded;
  }
}
