package be.mathiasbosman.entity;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "items")
public class Item extends AbstractAuditableEntity<UUID>{

  @Id
  @GeneratedValue
  private UUID id;
  private String name;

  public Item(String name) {
    this.name = name;
  }

  @Override
  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
