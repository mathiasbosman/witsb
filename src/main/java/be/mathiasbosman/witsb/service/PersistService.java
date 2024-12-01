package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.domain.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersistService {

  File updateFile(UUID reference, InputStream inputStream);

  File upload(String context, String name, InputStream inputStream);

  void deleteFile(UUID reference);

  Optional<File> findFile(UUID reference);

  Optional<File> findFile(UUID reference, int version);

  String toPath(File file);

  List<File> getAllVersions(UUID groupId);
}
