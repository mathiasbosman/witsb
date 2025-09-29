package be.mathiasbosman.witsb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.witsb.ContainerTest;
import be.mathiasbosman.witsb.domain.Artifact;
import be.mathiasbosman.witsb.domain.ArtifactMother;
import be.mathiasbosman.witsb.exception.EmptyFileException;
import be.mathiasbosman.witsb.exception.WitsbException;
import be.mathiasbosman.witsb.repository.ArtifactRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class ArtifactServiceImplTest extends ContainerTest {

  @Autowired
  private ArtifactServiceImpl persistService;
  @Autowired
  private ArtifactRepository artifactRepository;
  @MockBean
  private FileService fileService;

  private static InputStream toInputstream(String content) {
    return new ByteArrayInputStream(content.getBytes());
  }

  @Test
  void upload() {
    Artifact record = persistService.upload("contextA", "a.txt", toInputstream("content"));

    assertThat(record.getContext()).isEqualTo("contextA");
    assertThat(record.getFilename()).isEqualTo("a.txt");
    assertThat(record.getVersion()).isZero();

    Artifact artifact = artifactRepository.findByReference(record.getReference()).orElseThrow();

    assertThat(artifact.getId()).isNotNull();
    assertThat(artifact.getGroupId()).isNotNull();
    assertThat(artifact.getReference()).isEqualTo(record.getReference());
    assertThat(artifact.getVersion()).isEqualTo(record.getVersion());
    assertThat(artifact.getContext()).isEqualTo(record.getContext());
    assertThat(artifact.isLocked()).isFalse();
  }

  @Test
  void uploadAndLock() {
    Artifact artifact = persistService.uploadAndLock(UUID.randomUUID(), toInputstream("content"));

    assertThat(artifact.isLocked()).isTrue();
    assertThat(artifact.getLockGroupId()).isNotNull();
  }

  @Test
  void unlock() {
    UUID lockGroupId = UUID.randomUUID();
    Artifact artifactA = persistService.uploadAndLock(lockGroupId, toInputstream("content"));
    Artifact artifactB = persistService.uploadAndLock(lockGroupId, toInputstream("content_b"));

    List<Artifact> artifacts = persistService.unlock(lockGroupId);

    assertThat(artifacts)
        .hasSize(2)
        .contains(artifactA, artifactB);
    assertThat(artifactA.isLocked()).isFalse();
    assertThat(artifactB.isLocked()).isFalse();
  }

  @Test
  void upload_emptyFile() {
    try (InputStream emptyStream = toInputstream("")) {
      assertThatThrownBy(() -> persistService.upload("contextA", "a.txt", emptyStream))
          .isInstanceOf(EmptyFileException.class);
    } catch (IOException e) {
      fail("IOException should not be thrown");
    }
  }

  @Test
  void updateFile() {
    Artifact testArtifact = ArtifactMother.random();
    artifactRepository.save(testArtifact);
    Artifact newArtifact = persistService.updateFile(testArtifact.getReference(),
        toInputstream("new content"));
    Artifact updatedArtifact = artifactRepository.findByReference(newArtifact.getReference())
        .orElseThrow();

    assertThat(updatedArtifact.getGroupId()).isEqualTo(testArtifact.getGroupId());
    assertThat(updatedArtifact.getVersion()).isEqualTo(testArtifact.getVersion() + 1);
    assertThat(updatedArtifact.getReference()).isNotEqualTo(testArtifact.getReference());
    assertThat(updatedArtifact.isLocked()).isEqualTo(testArtifact.isLocked());
    assertThat(updatedArtifact.getLockGroupId()).isEqualTo(testArtifact.getLockGroupId());
  }

  @Test
  void updateFile_notFound() {
    try (InputStream is = toInputstream("foo")) {
      UUID notPersistedRef = UUID.randomUUID();

      assertThatThrownBy(() -> persistService.updateFile(notPersistedRef, is))
          .isInstanceOf(NoSuchElementException.class);
    } catch (IOException e) {
      fail("IOException should not be thrown");
    }
  }

  @Test
  void deleteFile() {
    Artifact testArtifact = ArtifactMother.random();
    Artifact testArtifact2 = ArtifactMother.withVersion(
        testArtifact.getGroupId(), testArtifact.getVersion() + 1);
    artifactRepository.saveAll(List.of(testArtifact, testArtifact2));
    ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
    doNothing().when(fileService).delete(pathCapture.capture());

    persistService.deleteFile(testArtifact2.getReference());

    assertThat(pathCapture.getValue()).isEqualTo(persistService.toPath(testArtifact2));
    assertThat(artifactRepository.findById(testArtifact.getId())).isEmpty();
    assertThat(artifactRepository.findById(testArtifact2.getId())).isEmpty();
  }

  @Test
  void purgeFiles() {
    LocalDateTime refDate = LocalDateTime.now();
    Artifact testArtifactA = ArtifactMother.withUploadedOn(refDate);
    Artifact testArtifactB = ArtifactMother.withUploadedOn(refDate.minusDays(1).minusSeconds(1));
    Artifact testArtifactC = ArtifactMother.withUploadedOn(refDate.minusDays(2));
    Artifact testArtifactD = ArtifactMother.withUploadedOn(refDate.plusDays(1));
    ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
    doNothing().when(fileService).delete(pathCapture.capture());
    artifactRepository.saveAll(List.of(testArtifactA, testArtifactB, testArtifactC, testArtifactD));

    persistService.purgeFiles();

    assertThat(artifactRepository.findById(testArtifactA.getId())).isPresent();
    assertThat(artifactRepository.findById(testArtifactB.getId())).isEmpty();
    assertThat(artifactRepository.findById(testArtifactC.getId())).isEmpty();
    assertThat(artifactRepository.findById(testArtifactD.getId())).isPresent();
    assertThat(pathCapture.getAllValues()).containsExactlyInAnyOrder(
        persistService.toPath(testArtifactB),
        persistService.toPath(testArtifactC));
  }

  @Test
  void findFile() {
    Artifact testArtifact = ArtifactMother.random();
    artifactRepository.save(testArtifact);

    Optional<Artifact> result = persistService.findFile(testArtifact.getReference());
    assertThat(result).isPresent()
        .hasValueSatisfying(
            file -> assertThat(file.getReference()).isEqualTo(testArtifact.getReference()));
  }

  @Test
  void findFile_ByVersion() {
    Artifact testArtifact = ArtifactMother.random();
    artifactRepository.save(testArtifact);

    assertThat(persistService.findFile(testArtifact.getReference(),
        testArtifact.getVersion())).isPresent();
    assertThat(persistService.findFile(testArtifact.getReference(), 99)).isEmpty();
  }

  @Test
  void getAllVersions() {
    UUID mockGroupId = UUID.randomUUID();
    Artifact artifact0 = ArtifactMother.withVersion(mockGroupId, 0);
    Artifact artifact1 = ArtifactMother.withVersion(mockGroupId, 1);
    Artifact artifact2 = ArtifactMother.withVersion(mockGroupId, 2);
    Artifact artifact3 = ArtifactMother.withVersion(mockGroupId, 3);
    artifactRepository.saveAll(List.of(artifact3, artifact2, artifact0, artifact1));

    List<Artifact> allVersions = persistService.getAllVersions(mockGroupId);

    assertThat(allVersions).containsExactly(artifact0, artifact1, artifact2, artifact3);
  }

  @Test
  void validateFile_failure() {
    try (InputStream mockIs = mock(InputStream.class)) {
      when(mockIs.available()).thenThrow(new IOException("Mock IOException"));

      assertThatThrownBy(() -> persistService.upload("contextA", "a.txt", mockIs))
          .isInstanceOf(WitsbException.class);
    } catch (IOException e) {
      // no op
    }
  }

}