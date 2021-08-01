package be.mathiasbosman.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Sort;

@ToString
@MappedSuperclass
public abstract class AbstractAuditableEntity<K> implements AuditableEntity, WitsbEntity<K> {

  public static final Sort DEFAULT_SORT = Sort.by("id");

  @Column(name = "created")
  private LocalDateTime created;
  @Column(name = "updated")
  private LocalDateTime updated;
  @PrePersist
  void prePersist() {
    created = LocalDateTime.now();
    updated = created;
  }

  @PreUpdate
  void preUpdate() {
    updated = LocalDateTime.now();
  }

  public LocalDateTime getCreated() {
    return created;
  }

  public LocalDateTime getUpdated() {
    return updated;
  }

  @Override
  public Sort getDefaultSort() {
    return DEFAULT_SORT;
  }
}
