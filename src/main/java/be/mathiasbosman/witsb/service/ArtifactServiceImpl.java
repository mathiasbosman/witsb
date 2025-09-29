package be.mathiasbosman.witsb.service;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.witsb.domain.Artifact;
import be.mathiasbosman.witsb.exception.EmptyFileException;
import be.mathiasbosman.witsb.exception.WitsbException;
import be.mathiasbosman.witsb.repository.ArtifactRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtifactServiceImpl implements ArtifactService {

  private final FileService fileService;
  private final ArtifactRepository artifactRepository;

  @Override
  @Transactional
  public Artifact upload(String context, String name, InputStream inputStream) {
    return saveFile(context, name, inputStream, 0, UUID.randomUUID(), null);
  }

  @Override
  @Transactional
  public Artifact updateFile(UUID reference, InputStream inputStream) {
    final Artifact latestArtifact = getLatestVersion(reference);
    return saveFile(latestArtifact.getContext(), latestArtifact.getFilename(), inputStream,
            latestArtifact.getVersion() + 1, latestArtifact.getGroupId(),
            latestArtifact.getLockGroupId());
  }

  @Override
  @Transactional
  public void deleteFile(UUID reference) {
    final Artifact artifact = artifactRepository.findByReference(reference).orElseThrow();
    artifactRepository.deleteByGroupId(artifact.getGroupId());
  }

  private void delete(Collection<Artifact> artifacts) {
    artifacts.forEach(file -> fileService.delete(toPath(file)));
    artifactRepository.deleteAll(artifacts);
  }

  /**
   * Upload a file into a lock group and lock it.
   *
   * @param lockedGroupId the locked group id
   * @param inputStream   the input stream
   * @return the uploaded file
   */
  @Transactional
  public Artifact uploadAndLock(UUID lockedGroupId, InputStream inputStream) {
    return saveFile(lockedGroupId.toString(),
        UUID.randomUUID().toString(),
        inputStream,
        0,
        UUID.randomUUID(),
        lockedGroupId);
  }

  /**
   * Unlock a file group.
   *
   * @param lockGroupId the lock group id
   * @return the list of unlocked files
   */
  @Transactional
  public List<Artifact> unlock(UUID lockGroupId) {
    List<Artifact> artifacts = artifactRepository.getByLockGroupId(lockGroupId);
    artifacts.forEach(file -> file.setLocked(false));
    return artifacts;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Artifact> findFile(UUID reference) {
    return artifactRepository.findByReference(reference);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Artifact> findFile(UUID reference, int version) {
    final Artifact artifact = artifactRepository.findByReference(reference).orElseThrow();
    return artifactRepository.findByGroupIdAndVersion(artifact.getGroupId(), version);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Artifact> getAllVersions(UUID groupId) {
    return artifactRepository.getByGroupId(groupId, Sort.by("version"));
  }

  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void purgeFiles() {
    LocalDateTime checkpoint = LocalDateTime.now().minusDays(1);
    log.info("Purging files before [{}]", checkpoint);
    delete(artifactRepository.findByUploadedOnBefore(checkpoint));
  }

  private Artifact getLatestVersion(UUID reference) {
    final Artifact artifact = artifactRepository.findByReference(reference).orElseThrow();
    return artifactRepository.getFirstByGroupIdOrderByVersionDesc(artifact.getGroupId());
  }

  private Artifact saveFile(String context, String name, InputStream inputStream, int version,
      UUID groupId, UUID lockedGroupId) {

    validateInputStream(inputStream);

    var file = new Artifact();
    file.setFilename(name);
    file.setVersion(version);
    file.setContext(context);
    file.setGroupId(groupId);
    file.setLockGroupId(lockedGroupId);
    file.setReference(UUID.randomUUID());
    file.setLocked(lockedGroupId != null);
    file.setUploadedOn(LocalDateTime.now());
    saveToFs(file, inputStream);
    return artifactRepository.save(file);
  }

  private void saveToFs(Artifact artifact, InputStream is) {
    fileService.save(is, toPath(artifact));
  }

  private void validateInputStream(InputStream inputStream) {
    try {
      if (inputStream.available() == 0) {
        throw new EmptyFileException("The file is empty");
      }
    } catch (IOException e) {
      throw new WitsbException("Error checking InputStream availability");
    }
  }
}
