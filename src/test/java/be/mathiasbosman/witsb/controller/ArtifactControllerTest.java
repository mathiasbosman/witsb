package be.mathiasbosman.witsb.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.witsb.domain.Artifact;
import be.mathiasbosman.witsb.domain.ArtifactMother;
import be.mathiasbosman.witsb.domain.EventPayload;
import be.mathiasbosman.witsb.exception.EmptyFileException;
import be.mathiasbosman.witsb.service.ArtifactServiceImpl;
import be.mathiasbosman.witsb.service.EventService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest
class ArtifactControllerTest {

  private static final String mockContent = "some xml";
  private static final InputStream is = new ByteArrayInputStream(mockContent.getBytes());
  @Autowired
  private MockMvc mvc;
  @MockBean
  private FileService fileService;
  @MockBean
  private EventService eventService;
  @MockBean
  private ArtifactServiceImpl persistService;

  @Captor
  private ArgumentCaptor<EventPayload> wsMessageCaptor;

  private static MockMultipartFile mockMultiPartFile() throws Exception {
    return new MockMultipartFile("file", "filename.txt", "text/plain", is);
  }

  private static RequestPostProcessor putRequest() {
    return request -> {
      request.setMethod("PUT");
      return request;
    };
  }

  @Test
  void upload() throws Exception {
    when(persistService.upload(any(), any(), any())).thenReturn(ArtifactMother.random());

    mvc.perform(MockMvcRequestBuilders.multipart("/api/bar")
            .file(mockMultiPartFile()))
        .andExpect(status().isOk());

    verify(persistService).upload(eq("bar"), eq("file"), any(InputStream.class));
  }

  @Test
  void upload_emptyFile() throws Exception {
    when(persistService.upload(any(), any(), any())).thenThrow(new EmptyFileException("foo"));

    mvc.perform(MockMvcRequestBuilders.multipart("/api/bar").file(mockMultiPartFile()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title", is("Empty file")))
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void upload_largeFile() throws Exception {
    byte[] largeFileContent = new byte[10 * 1024 * 1024]; // 10MB file
    MockMultipartFile largeFile = new MockMultipartFile("file", "largefile.txt", "text/plain",
        largeFileContent);

    when(persistService.upload(any(), any(), any())).thenReturn(ArtifactMother.random());

    mvc.perform(MockMvcRequestBuilders.multipart("/api/bar")
            .file(largeFile))
        .andExpect(status().isOk());

    verify(persistService).upload(eq("bar"), eq("file"), any(InputStream.class));
  }

  @Test
  void lock() throws Exception {
    UUID lockedGroupId = UUID.randomUUID();
    when(persistService.uploadAndLock(eq(lockedGroupId), any())).thenReturn(
            ArtifactMother.random());

    mvc.perform(multipart("/api/lock")
            .file(mockMultiPartFile())
            .param("lockedGroupId", lockedGroupId.toString())
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isAccepted());

    verify(persistService).uploadAndLock(eq(lockedGroupId), any());

  }

  @Test
  void unlock() throws Exception {
    UUID lockGroupId = UUID.randomUUID();

    mvc.perform(MockMvcRequestBuilders.post("/api/unlock/" + lockGroupId))
        .andExpect(status().isOk());

    verify(persistService).unlock(lockGroupId);
    verify(eventService).sendMessage(wsMessageCaptor.capture());

    EventPayload wsMessage = wsMessageCaptor.getValue();
    assertThat(wsMessage.topic()).isEqualTo(
            ArtifactController.WS_TOPIC_UNLOCKED + "/" + lockGroupId);
  }

  @Test
  void update() throws Exception {
    UUID mockReference = UUID.randomUUID();
    when(persistService.updateFile(eq(mockReference), any(InputStream.class)))
            .thenReturn(ArtifactMother.random());

    mvc.perform(MockMvcRequestBuilders.multipart("/api/" + mockReference)
            .file(mockMultiPartFile())
            .with(putRequest()))
        .andExpect(status().isOk());

    verify(persistService).updateFile(eq(mockReference), any(InputStream.class));
  }

  @Test
  void update_emptyFile() throws Exception {
    UUID mockReference = UUID.randomUUID();
    when(persistService.updateFile(eq(mockReference), any(InputStream.class)))
        .thenThrow(new EmptyFileException("foo"));

    mvc.perform(MockMvcRequestBuilders.multipart("/api/" + mockReference)
            .file(mockMultiPartFile())
            .with(putRequest()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title", is("Empty file")))
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void download_unlocked() throws Exception {
    Artifact mockArtifact = ArtifactMother.withLocked("file.xml", false);
    mockDownload(mockArtifact);

    mvc.perform(get("/api/" + mockArtifact.getReference()))
        .andExpectAll(
            status().isOk(),
            header().string(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(mockArtifact.getFilename()).build()
                    .toString()),
            header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE),
            content().contentType(MediaType.APPLICATION_XML));
  }

  @Test
  void download_locked() throws Exception {
    Artifact mockArtifact = ArtifactMother.withLocked("file.xml", true);
    mockDownload(mockArtifact);

    mvc.perform(get("/api/" + mockArtifact.getReference()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void download_InternalServerError() throws Exception {
    try (MockedStatic<IOUtils> mockedIOUtils = Mockito.mockStatic(IOUtils.class)) {
      mockedIOUtils.when(() -> IOUtils.copy(any(InputStream.class), any(OutputStream.class)))
          .thenThrow(new IOException("Mocked IOException"));
      Artifact mockArtifact = ArtifactMother.random();
      mockDownload(mockArtifact);

      mvc.perform(get("/api/" + mockArtifact.getReference()))
          .andExpect(status().isInternalServerError());
    }
  }

  @Test
  void download_NotFound() throws Exception {
    when(persistService.findFile(any())).thenReturn(Optional.empty());

    mvc.perform(get("/api/" + UUID.randomUUID()))
        .andExpectAll(status().isNotFound());
  }

  @Test
  void download_ByVersion() throws Exception {
    Artifact mockArtifact = ArtifactMother.random();
    mockDownload(mockArtifact);
    when(persistService.findFile(mockArtifact.getReference(), mockArtifact.getVersion()))
            .thenReturn(Optional.of(mockArtifact));

    mvc.perform(get("/api/" + mockArtifact.getReference()).param("version", "0"))
        .andExpect(status().isOk());
  }

  @Test
  void download_invalidUUID() throws Exception {
    mvc.perform(get("/api/invalid-uuid"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void delete() throws Exception {
    UUID mockReference = UUID.randomUUID();

    mvc.perform(MockMvcRequestBuilders.delete("/api/" + mockReference));

    verify(persistService).deleteFile(mockReference);
  }

  @Test
  void delete_invalidUUID() throws Exception {
    mvc.perform(MockMvcRequestBuilders.delete("/api/invalid-uuid"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void listGroup() throws Exception {
    Artifact artifactA = ArtifactMother.random();
    Artifact artifactB = ArtifactMother.random();
    Artifact artifactC = ArtifactMother.random();

    when(persistService.getAllVersions(any()))
            .thenReturn(List.of(artifactA, artifactB, artifactC));

    mvc.perform(get("/api/group/" + UUID.randomUUID()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].reference", is(artifactA.getReference().toString())))
            .andExpect(jsonPath("$[1].reference", is(artifactB.getReference().toString())))
            .andExpect(jsonPath("$[2].reference", is(artifactC.getReference().toString())));
  }

  private void mockDownload(Artifact artifact) {
    when(persistService.findFile(artifact.getReference()))
            .thenReturn(Optional.of(artifact));
    when(persistService.toPath(any()))
        .thenReturn("path/to/file");
    when(fileService.open("path/to/file"))
        .thenReturn(is);
  }

}
