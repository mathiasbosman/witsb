package be.mathiasbosman.witsb.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "files")
public class File extends AbstractAuditedEntity {

  @Id
  @GeneratedValue
  @Column(nullable = false)
  private UUID id;
  @Column(nullable = false, updatable = false)
  private UUID reference;
  @Column(nullable = false)
  private String filename;
  @Column(nullable = false)
  private String context;
  @Column(nullable = false, updatable = false)
  private int version = 0;
  @Column(nullable = false, updatable = false)
  private UUID groupId;
  private boolean locked;
  private UUID lockGroupId;
  private LocalDateTime uploadedOn;
}
