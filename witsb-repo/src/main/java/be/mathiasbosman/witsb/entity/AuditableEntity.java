package be.mathiasbosman.witsb.entity;

import java.time.LocalDateTime;

public interface AuditableEntity {

  LocalDateTime getCreated();

  LocalDateTime getUpdated();
}
