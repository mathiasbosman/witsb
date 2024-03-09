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
import be.mathiasbosman.witsb.domain.FileRecord;
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
    FileRecord record = persistService.upload("contextA", "a.txt", toInputstream("content"));

    assertEquals("contextA", record.context());
    assertEquals("a.txt", record.fileName());
    assertEquals(0, record.version());

    File file = fileRepository.findByReference(record.reference()).orElseThrow();

    assertNotNull(file.getId());
    assertNotNull(file.getGroupId());
    assertEquals(file.getReference(), record.reference());
    assertEquals(file.getVersion(), record.version());
    assertEquals(file.getContext(), record.context());
  }

  @Test
  void updateFile() {
    File testFile = createMockFile();
    fileRepository.save(testFile);
    File notPersisted = createMockFile();
    FileRecord newFile = persistService.updateFile(testFile.getReference(),
        toInputstream("new content"));
    File updatedFile = fileRepository.findByReference(newFile.reference()).orElseThrow();

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
    File testFile = createMockFile();
    File testFile2 = createMockFile().toBuilder()
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
    File testFile = createMockFile();
    fileRepository.save(testFile);

    Optional<File> result = persistService.findFile(testFile.getReference());
    assertTrue(result.isPresent());
    assertEquals(testFile.getReference(), result.get().getReference());
  }

  @Test
  void findFile_ByVersion() {
    File testFile = createMockFile();
    fileRepository.save(testFile);

    assertTrue(persistService.findFile(testFile.getReference(), testFile.getVersion()).isPresent());
    assertTrue(persistService.findFile(testFile.getReference(), 99).isEmpty());
  }

  private File createMockFile() {
    return File.builder()
        .context("foo")
        .groupId(UUID.randomUUID())
        .filename("bar")
        .build();
  }
}