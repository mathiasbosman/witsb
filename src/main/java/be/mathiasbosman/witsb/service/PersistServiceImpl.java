package be.mathiasbosman.witsb.service;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.fs.core.util.FileServiceUtils;
import be.mathiasbosman.witsb.domain.File;
import be.mathiasbosman.witsb.domain.FileRecord;
import be.mathiasbosman.witsb.repository.FileRepository;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistServiceImpl implements PersistService {

  private final FileService fileService;
  private final FileRepository fileRepository;

  static FileRecord fromEntity(File file) {
    return new FileRecord(file.getFilename(), file.getContext(), file.getReference(),
        file.getVersion());
  }

  @Override
  @Transactional
  public FileRecord upload(String context, String name, InputStream inputStream) {
    File file = saveFile(context, name, inputStream, 0, UUID.randomUUID());
    return fromEntity(file);
  }

  @Override
  @Transactional
  public FileRecord updateFile(UUID reference, InputStream inputStream) {
    File latestlFile = getLatestVersion(reference);
    int newVersion = latestlFile.getVersion() + 1;
    File newFile = saveFile(latestlFile.getContext(), latestlFile.getFilename(), inputStream,
        newVersion, latestlFile.getGroupId());
    return fromEntity(newFile);
  }

  @Override
  @Transactional
  public void deleteFile(UUID reference) {
    File file = findFile(reference).orElseThrow();
    fileRepository.findByGroupId(file.getGroupId())
        .forEach(this::delete);
  }

  private void delete(File file) {
    log.info("Deleting {}/{}", file.getGroupId(), file.getReference());
    try {
      fileService.delete(toPath(file));
    } catch (IllegalArgumentException e) {
      log.trace("Error when deleting {} on the filesystem (ignoring)", file.getReference(), e);
    } finally {
      fileRepository.delete(file);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<File> findFile(UUID reference) {
    return fileRepository.findByReference(reference);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<File> findFile(UUID reference, int version) {
    File file = fileRepository.findByReference(reference).orElseThrow();
    return fileRepository.findByGroupIdAndVersion(file.getGroupId(), version);
  }

  @Override
  @Transactional(readOnly = true)
  public File getLatestVersion(UUID reference) {
    File file = findFile(reference).orElseThrow();
    return fileRepository.getFirstByGroupIdOrderByVersionDesc(file.getGroupId());
  }

  @Override
  public String toPath(File file) {
    return FileServiceUtils.combine(file.getContext(), file.getReference().toString());
  }

  private File saveFile(String context, String name, InputStream inputStream, int version,
      UUID groupId) {
    File file = new File();
    file.setContext(context);
    file.setFilename(name);
    file.setVersion(version);
    file.setGroupId(groupId);
    saveToFs(file, inputStream);
    return fileRepository.save(file);
  }

  private void saveToFs(File file, InputStream is) {
    fileService.save(is, toPath(file));
  }
}
