package be.mathiasbosman.witsb.domain;

import java.time.Instant;
import java.util.UUID;

public record FileRecord(String fileName, String context, UUID reference, int version,
                         boolean locked,
                         Instant created, Instant updated) {

  public static FileRecord fromEntity(File file) {
    return new FileRecord(file.getFilename(), file.getContext(), file.getReference(),
        file.getVersion(), file.isLocked(), file.getCreatedOn(), file.getUpdatedOn());
  }
}
