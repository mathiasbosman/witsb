package be.mathiasbosman.witsb.controller;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.fs.core.util.FileServiceUtils;
import be.mathiasbosman.witsb.domain.File;
import be.mathiasbosman.witsb.domain.FileRecord;
import be.mathiasbosman.witsb.service.PersistService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class FileController {

  private final PersistService persistService;
  private final FileService fileService;

  @PostMapping("/{context}")
  public @ResponseBody FileRecord upload(@PathVariable String context,
      @RequestParam("file") MultipartFile multipartFile)
      throws IOException {
    return persistService.upload(context, multipartFile.getName(), multipartFile.getInputStream());
  }

  @PutMapping("/{reference}")
  public @ResponseBody FileRecord update(@PathVariable UUID reference,
      @RequestParam("file") MultipartFile multipartFile)
      throws IOException {
    return persistService.updateFile(reference, multipartFile.getInputStream());
  }

  @GetMapping("/{reference}")
  public void download(@PathVariable UUID reference,
      @RequestParam(name = "v", required = false) Integer version,
      HttpServletResponse response) {
    if (version != null) {
      File file = persistService.findFile(reference, version).orElseThrow();
      this.download(file.getReference(), response);
    } else {
      this.download(reference, response);
    }
  }

  private void download(UUID reference, HttpServletResponse response) {
    persistService.findFile(reference).ifPresentOrElse(file -> this.writeFileStream(file, response),
        () -> {
          log.error("No file found for {}", reference);
          response.setStatus(HttpStatus.NOT_FOUND.value());
        });
  }


  @DeleteMapping("/{reference}")
  public void delete(@PathVariable UUID reference) {
    persistService.deleteFile(reference);
  }


  void writeFileStream(File file, HttpServletResponse response) {
    ContentDisposition disposition = ContentDisposition.attachment().filename(file.getFilename())
        .build();
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
    response.setHeader(HttpHeaders.CONTENT_TYPE, FileServiceUtils.getContentType(file.getFilename(),
        MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE));
    InputStream inputStream = fileService.open(persistService.toPath(file));
    try {
      IOUtils.copy(inputStream, response.getOutputStream());
    } catch (IOException e) {
      log.error("Error writing to output stream for {}", file.getReference());
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }
}
