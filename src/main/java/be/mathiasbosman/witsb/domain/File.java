package be.mathiasbosman.witsb.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "files")
public class File {

  @Id
  @GeneratedValue
  @Column(nullable = false)
  private UUID id;
  @Builder.Default
  @Column(nullable = false, updatable = false)
  private UUID reference = UUID.randomUUID();
  @Column(nullable = false)
  private String filename;
  @Column(nullable = false)
  private String context;
  @Builder.Default
  @Column(nullable = false, updatable = false)
  private int version = 0;
  @Column(nullable = false, updatable = false)
  private UUID groupId;
}
