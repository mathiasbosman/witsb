package be.mathiasbosman.witsb.entity;

import java.util.UUID;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.domain.Sort;

@MappedSuperclass
public class AbstractWitsbEntity extends AbstractAuditableEntity implements WitsbEntity<UUID> {

  public static final Sort DEFAULT_SORT = Sort.by("id");

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "uuid2")
  private UUID id;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Sort getDefaultSort() {
    return DEFAULT_SORT;
  }
}
