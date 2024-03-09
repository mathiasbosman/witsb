package be.mathiasbosman.witsb.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.witsb.domain.File;
import be.mathiasbosman.witsb.service.PersistServiceImpl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
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
class FileControllerTest {

  private static final String mockContent = "some xml";
  private static final InputStream is = new ByteArrayInputStream(mockContent.getBytes());
  @Autowired
  private MockMvc mvc;
  @MockBean
  private FileService fileService;
  @MockBean
  private PersistServiceImpl persistService;

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
    when(persistService.upload(any(), any(), any())).thenReturn(mockFile());

    mvc.perform(MockMvcRequestBuilders.multipart("/api/bar")
            .file(mockMultiPartFile()))
        .andExpect(status().isOk());

    verify(persistService).upload(eq("bar"), eq("file"), any(InputStream.class));
  }

  @Test
  void update() throws Exception {
    final UUID mockReference = UUID.randomUUID();
    when(persistService.updateFile(eq(mockReference), any(InputStream.class)))
        .thenReturn(mockFile());

    mvc.perform(MockMvcRequestBuilders.multipart("/api/" + mockReference)
            .file(mockMultiPartFile())
            .with(putRequest()))
        .andExpect(status().isOk());

    verify(persistService).updateFile(eq(mockReference), any(InputStream.class));
  }

  @Test
  void download() throws Exception {
    final File mockFile = File.builder()
        .filename("file.xml")
        .build();
    mockDownload(mockFile);

    mvc.perform(get("/api/" + mockFile.getReference()))
        .andExpectAll(
            status().isOk(),
            header().string(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(mockFile.getFilename()).build()
                    .toString()),
            header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE),
            content().contentType(MediaType.APPLICATION_XML));
  }

  @Test
  void download_InternalServerError() throws Exception {
    try (MockedStatic<IOUtils> mockedIOUtils = Mockito.mockStatic(IOUtils.class)) {
      mockedIOUtils.when(() -> IOUtils.copy(any(InputStream.class), any(OutputStream.class)))
          .thenThrow(new IOException("Mocked IOException"));
      final File mockFile = mockFile();
      mockDownload(mockFile);

      mvc.perform(get("/api/" + mockFile.getReference()))
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
    final File mockFile = mockFile();
    mockDownload(mockFile);
    when(persistService.findFile(mockFile.getReference(), mockFile.getVersion()))
        .thenReturn(Optional.of(mockFile));

    mvc.perform(get("/api/" + mockFile.getReference()).param("version", "0"))
        .andExpect(status().isOk());
  }

  @Test
  void delete() throws Exception {
    final UUID mockReference = UUID.randomUUID();

    mvc.perform(MockMvcRequestBuilders.delete("/api/" + mockReference));

    verify(persistService).deleteFile(mockReference);
  }

  @Test
  void listGroup() throws Exception {
    final File fileA = mockFile();
    final File fileB = mockFile();
    final File fileC = mockFile();

    when(persistService.getAllVersions(any()))
        .thenReturn(List.of(fileA, fileB, fileC));

    mvc.perform(get("/api/group/" + UUID.randomUUID()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].reference", is(fileA.getReference().toString())))
        .andExpect(jsonPath("$[1].reference", is(fileB.getReference().toString())))
        .andExpect(jsonPath("$[2].reference", is(fileC.getReference().toString())));
  }

  private File mockFile() {
    return File.builder()
        .filename("foo.txt")
        .build();
  }

  private void mockDownload(File file) {
    when(persistService.findFile(file.getReference()))
        .thenReturn(Optional.of(file));
    when(persistService.toPath(any()))
        .thenReturn("path/to/file");
    when(fileService.open("path/to/file"))
        .thenReturn(is);
  }

}
