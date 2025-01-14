package be.mathiasbosman.witsb.domain;

import java.time.LocalDateTime;
import java.util.UUID;


public record FileRecord(String fileName, String context, UUID reference, int version,
                         boolean locked, UUID lockGroupId, LocalDateTime uploaded) {

  /**
   * Converts a File entity to a FileRecord.
   *
   * @param file the entity to convert
   * @return the converted record
   */
  public static FileRecord fromEntity(File file) {
    return new FileRecord(
        file.getFilename(),
        file.getContext(),
        file.getReference(),
        file.getVersion(),
        file.isLocked(),
        file.getLockGroupId(),
        file.getUploadedOn());
  }
}
