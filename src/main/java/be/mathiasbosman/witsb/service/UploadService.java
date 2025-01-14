package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.domain.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadService {

  /**
   * Upload a file.
   *
   * @param context     the context
   * @param name        the name
   * @param inputStream the input stream
   * @return the uploaded file
   */
  File upload(String context, String name, InputStream inputStream);

  /**
   * Update a file.
   *
   * @param reference   the reference
   * @param inputStream the input stream
   * @return the updated file
   */
  File updateFile(UUID reference, InputStream inputStream);

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
   * @see UploadService#findFile(UUID, int)
   */
  Optional<File> findFile(UUID reference);

  /**
   * Find a file by reference and version.
   *
   * @param reference the reference
   * @param version   the version
   * @return the file
   * @see UploadService#findFile(UUID)
   */
  Optional<File> findFile(UUID reference, int version);

  /**
   * Creates the path on the file system from a file.
   *
   * @param file the file
   * @return the path
   */
  String toPath(File file);

  /**
   * Get all versions via the group id.
   *
   * @param groupId the group id
   * @return the list of files
   */
  List<File> getAllVersions(UUID groupId);
}
