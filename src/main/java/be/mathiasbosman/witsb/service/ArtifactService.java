package be.mathiasbosman.witsb.service;

import be.mathiasbosman.fs.core.util.FileServiceUtils;
import be.mathiasbosman.witsb.domain.Artifact;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;

public interface ArtifactService {

  /**
   * Upload a file.
   *
   * @param context     the context
   * @param name        the name
   * @param inputStream the input stream
   * @return the uploaded file
   */
  Artifact upload(String context, String name, InputStream inputStream);

  /**
   * Update a file.
   *
   * @param reference   the reference
   * @param inputStream the input stream
   * @return the updated file
   */
  Artifact updateFile(UUID reference, InputStream inputStream);

  /**
   * Delete a file.
   *
   * @param reference the reference
   */
  void deleteFile(UUID reference);

  /**
   * Find a file.
   *
   * @param reference the reference
   * @return the file
   * @see ArtifactService#findFile(UUID, int)
   */
  Optional<Artifact> findFile(UUID reference);

  /**
   * Find a file by reference and version.
   *
   * @param reference the reference
   * @param version   the version
   * @return the file
   * @see ArtifactService#findFile(UUID)
   */
  Optional<Artifact> findFile(UUID reference, int version);

  /**
   * Get all versions via the group id.
   *
   * @param groupId the group id
   * @return the list of files
   */
  List<Artifact> getAllVersions(UUID groupId);

  default String toPath(@NonNull Artifact artifact) {
    return FileServiceUtils.combine(artifact.getContext(), artifact.getReference().toString());
  }
}
