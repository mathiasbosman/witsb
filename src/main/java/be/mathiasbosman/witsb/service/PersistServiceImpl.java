package be.mathiasbosman.witsb.service;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.fs.core.util.FileServiceUtils;
import be.mathiasbosman.witsb.domain.File;
import be.mathiasbosman.witsb.exception.EmptyFileException;
import be.mathiasbosman.witsb.repository.FileRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistServiceImpl implements PersistService {

  private final FileService fileService;
  private final FileRepository fileRepository;

  @Override
  @Transactional
  public File upload(String context, String name, InputStream inputStream) {
    return saveFile(context, name, inputStream, 0, UUID.randomUUID());
  }

  @Override
  @Transactional
  public File updateFile(UUID reference, InputStream inputStream) {
    final File latestFile = getLatestVersion(reference);
    return saveFile(latestFile.getContext(), latestFile.getFilename(), inputStream,
        latestFile.getVersion() + 1, latestFile.getGroupId());
  }

  @Override
  @Transactional
  public void deleteFile(UUID reference) {
    final File file = fileRepository.findByReference(reference).orElseThrow();
    fileRepository.getByGroupId(file.getGroupId()).forEach(this::delete);
  }

  private void delete(File file) {
    log.info("Deleting {}/{}", file.getGroupId(), file.getReference());
    fileService.delete(toPath(file));
    fileRepository.delete(file);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<File> findFile(UUID reference) {
    return fileRepository.findByReference(reference);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<File> findFile(UUID reference, int version) {
    final File file = fileRepository.findByReference(reference).orElseThrow();
    return fileRepository.findByGroupIdAndVersion(file.getGroupId(), version);
  }

  @Override
  public String toPath(File file) {
    return FileServiceUtils.combine(file.getContext(), file.getReference().toString());
  }

  @Override
  @Transactional(readOnly = true)
  public List<File> getAllVersions(UUID groupId) {
    return fileRepository.getByGroupId(groupId, Sort.by("version"));
  }

  private File getLatestVersion(UUID reference) {
    final File file = fileRepository.findByReference(reference).orElseThrow();
    return fileRepository.getFirstByGroupIdOrderByVersionDesc(file.getGroupId());
  }

  private File saveFile(String context, String name, InputStream inputStream, int version,
      UUID groupId) {

    validateFile(inputStream);

    var file = new File();
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

  private void validateFile(InputStream inputStream) {
    try {
      if (inputStream.available() == 0) {
        throw new EmptyFileException("The file is empty");
      }
    } catch (IOException e) {
      throw new RuntimeException("Error checking InputStream availability");
    }
  }
}
