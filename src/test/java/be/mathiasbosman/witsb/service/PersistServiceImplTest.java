package be.mathiasbosman.witsb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.witsb.ContainerTest;
import be.mathiasbosman.witsb.domain.File;
import be.mathiasbosman.witsb.domain.FileMother;
import be.mathiasbosman.witsb.exception.EmptyFileException;
import be.mathiasbosman.witsb.repository.FileRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class PersistServiceImplTest extends ContainerTest {

  @Autowired
  private PersistServiceImpl persistService;
  @Autowired
  private FileRepository fileRepository;
  @MockBean
  private FileService fileService;

  private static InputStream toInputstream(String content) {
    return new ByteArrayInputStream(content.getBytes());
  }

  @Test
  void upload() {
    File record = persistService.upload("contextA", "a.txt", toInputstream("content"));

    assertThat(record.getContext()).isEqualTo("contextA");
    assertThat(record.getFilename()).isEqualTo("a.txt");
    assertThat(record.getVersion()).isZero();

    File file = fileRepository.findByReference(record.getReference()).orElseThrow();

    assertThat(file.getId()).isNotNull();
    assertThat(file.getGroupId()).isNotNull();
    assertThat(file.getReference()).isEqualTo(record.getReference());
    assertThat(file.getVersion()).isEqualTo(record.getVersion());
    assertThat(file.getContext()).isEqualTo(record.getContext());
    assertThat(file.isLocked()).isFalse();
  }

  @Test
  void uploadAndLock() {
    File file = persistService.uploadAndLock(UUID.randomUUID(), toInputstream("content"));

    assertThat(file.isLocked()).isTrue();
    assertThat(file.getLockGroupId()).isNotNull();
  }

  @Test
  void unlock() {
    UUID lockGroupId = UUID.randomUUID();
    File fileA = persistService.uploadAndLock(lockGroupId, toInputstream("content"));
    File fileB = persistService.uploadAndLock(lockGroupId, toInputstream("content_b"));

    persistService.unlock(lockGroupId);

    assertThat(fileA.isLocked()).isFalse();
    assertThat(fileB.isLocked()).isFalse();
  }

  @Test
  void upload_emptyFile() {
    InputStream emptyStream = toInputstream("");

    assertThatThrownBy(() -> persistService.upload("contextA", "a.txt", emptyStream))
        .isInstanceOf(EmptyFileException.class);
  }

  @Test
  void updateFile() {
    File testFile = FileMother.random();
    fileRepository.save(testFile);
    File newFile = persistService.updateFile(testFile.getReference(), toInputstream("new content"));
    File updatedFile = fileRepository.findByReference(newFile.getReference()).orElseThrow();

    assertThat(updatedFile.getGroupId()).isEqualTo(testFile.getGroupId());
    assertThat(updatedFile.getVersion()).isEqualTo(testFile.getVersion() + 1);
    assertThat(updatedFile.getReference()).isNotEqualTo(testFile.getReference());
    assertThat(updatedFile.isLocked()).isEqualTo(testFile.isLocked());
    assertThat(updatedFile.getLockGroupId()).isEqualTo(testFile.getLockGroupId());
  }

  @Test
  void updateFile_notFound() {
    InputStream is = toInputstream("foo");
    UUID notPersistedRef = UUID.randomUUID();

    assertThatThrownBy(() -> persistService.updateFile(notPersistedRef, is))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void deleteFile() {
    File testFile = FileMother.random();
    File testFile2 = FileMother.of(testFile.getGroupId(), testFile.getVersion() + 1);
    fileRepository.saveAll(List.of(testFile, testFile2));
    ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
    doNothing().when(fileService).delete(pathCapture.capture());

    persistService.deleteFile(testFile2.getReference());

    assertThat(pathCapture.getValue()).isEqualTo(persistService.toPath(testFile2));
    assertThat(fileRepository.findById(testFile.getId())).isEmpty();
    assertThat(fileRepository.findById(testFile2.getId())).isEmpty();
  }

  @Test
  void findFile() {
    File testFile = FileMother.random();
    fileRepository.save(testFile);

    Optional<File> result = persistService.findFile(testFile.getReference());
    assertThat(result).isPresent()
        .hasValueSatisfying(
            file -> assertThat(file.getReference()).isEqualTo(testFile.getReference()));
  }

  @Test
  void findFile_ByVersion() {
    File testFile = FileMother.random();
    fileRepository.save(testFile);

    assertThat(persistService.findFile(testFile.getReference(), testFile.getVersion())).isPresent();
    assertThat(persistService.findFile(testFile.getReference(), 99)).isEmpty();
  }

  @Test
  void getAllVersions() {
    UUID mockGroupId = UUID.randomUUID();
    File file0 = FileMother.of(mockGroupId, 0);
    File file1 = FileMother.of(mockGroupId, 1);
    File file2 = FileMother.of(mockGroupId, 2);
    File file3 = FileMother.of(mockGroupId, 3);
    fileRepository.saveAll(List.of(file3, file2, file0, file1));

    List<File> allVersions = persistService.getAllVersions(mockGroupId);

    assertThat(allVersions).containsExactly(file0, file1, file2, file3);
  }

  @Test
  void validateFile_failure() {
    try (InputStream mockIs = mock(InputStream.class)) {
      when(mockIs.available()).thenThrow(new IOException("Mock IOException"));

      assertThatThrownBy(() -> persistService.upload("contextA", "a.txt", mockIs))
          .isInstanceOf(RuntimeException.class);
    } catch (IOException e) {
      // no op
    }
  }

}