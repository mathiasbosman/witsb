package be.mathiasbosman.entity;

import java.time.LocalDateTime;

public interface AuditableEntity {

  LocalDateTime getCreated();

  LocalDateTime getUpdated();
}
