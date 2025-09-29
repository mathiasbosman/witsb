package be.mathiasbosman.witsb.domain;

import java.time.LocalDateTime;
import java.util.UUID;


public record ArtifactDto(String fileName, String context, UUID reference, int version,
                          boolean locked, UUID lockGroupId, LocalDateTime uploaded) {

  /**
   * Converts a File entity to a FileRecord.
   *
   * @param artifact the entity to convert
   * @return the converted record
   */
  public static ArtifactDto fromEntity(Artifact artifact) {
    return new ArtifactDto(
        artifact.getFilename(),
        artifact.getContext(),
        artifact.getReference(),
        artifact.getVersion(),
        artifact.isLocked(),
        artifact.getLockGroupId(),
        artifact.getUploadedOn());
  }
}
