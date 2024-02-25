package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.domain.File;
import be.mathiasbosman.witsb.domain.FileRecord;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

public interface PersistService {

  FileRecord updateFile(UUID reference, InputStream inputStream);

  FileRecord upload(String context, String name, InputStream inputStream);

  void deleteFile(UUID reference);

  Optional<File> findFile(UUID reference);

  File getLatestVersion(UUID reference);

  Optional<File> findFile(UUID reference, int version);

  String toPath(File file);
}
