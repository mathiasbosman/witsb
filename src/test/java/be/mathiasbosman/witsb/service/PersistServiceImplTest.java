package be.mathiasbosman.witsb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.witsb.ContainerTest;
import be.mathiasbosman.witsb.domain.File;
import be.mathiasbosman.witsb.repository.FileRepository;
import java.io.ByteArrayInputStream;
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
    final File record = persistService.upload("contextA", "a.txt", toInputstream("content"));

    assertEquals("contextA", record.getContext());
    assertEquals("a.txt", record.getFilename());
    assertEquals(0, record.getVersion());

    final File file = fileRepository.findByReference(record.getReference()).orElseThrow();

    assertNotNull(file.getId());
    assertNotNull(file.getGroupId());
    assertEquals(file.getReference(), record.getReference());
    assertEquals(file.getVersion(), record.getVersion());
    assertEquals(file.getContext(), record.getContext());
  }

  @Test
  void updateFile() {
    final File testFile = createMockFile();
    fileRepository.save(testFile);
    final File notPersisted = createMockFile();
    final File newFile = persistService.updateFile(testFile.getReference(),
        toInputstream("new content"));
    final File updatedFile = fileRepository.findByReference(newFile.getReference()).orElseThrow();

    assertEquals(testFile.getGroupId(), updatedFile.getGroupId());
    assertEquals(testFile.getVersion() + 1, updatedFile.getVersion());
    assertNotEquals(testFile.getReference(), updatedFile.getReference());
    InputStream is = toInputstream("foo");
    UUID notPersistedRef = notPersisted.getReference();
    assertThrows(NoSuchElementException.class,
        () -> persistService.updateFile(notPersistedRef, is));
  }

  @Test
  void deleteFile() {
    final File testFile = createMockFile();
    final File testFile2 = createMockFile().toBuilder()
        .groupId(testFile.getGroupId())
        .version(testFile.getVersion() + 1)
        .build();
    fileRepository.saveAll(List.of(testFile, testFile2));
    ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
    doNothing().when(fileService).delete(pathCapture.capture());

    persistService.deleteFile(testFile2.getReference());

    assertEquals(persistService.toPath(testFile2), pathCapture.getValue());
    assertTrue(fileRepository.findById(testFile.getId()).isEmpty());
    assertTrue(fileRepository.findById(testFile2.getId()).isEmpty());
  }

  @Test
  void findFile() {
    final File testFile = createMockFile();
    fileRepository.save(testFile);

    Optional<File> result = persistService.findFile(testFile.getReference());
    assertTrue(result.isPresent());
    assertEquals(testFile.getReference(), result.get().getReference());
  }

  @Test
  void findFile_ByVersion() {
    final File testFile = createMockFile();
    fileRepository.save(testFile);

    assertTrue(persistService.findFile(testFile.getReference(), testFile.getVersion()).isPresent());
    assertTrue(persistService.findFile(testFile.getReference(), 99).isEmpty());
  }

  @Test
  void getAllVersions() {
    UUID mockGroupId = UUID.randomUUID();
    final File file0 = createMockFile(mockGroupId, 0);
    final File file1 = createMockFile(mockGroupId, 1);
    final File file2 = createMockFile(mockGroupId, 2);
    final File file3 = createMockFile(mockGroupId, 3);
    fileRepository.saveAll(List.of(file3, file2, file0, file1));

    List<File> allVersions = persistService.getAllVersions(mockGroupId);

    assertEquals(List.of(file0, file1, file2, file3), allVersions);
  }

  private File createMockFile(UUID groupId, int version) {
    return File.builder()
        .context("foo")
        .groupId(groupId)
        .version(version)
        .filename("bar")
        .build();
  }

  private File createMockFile() {
    return createMockFile(UUID.randomUUID(), 0);
  }
}