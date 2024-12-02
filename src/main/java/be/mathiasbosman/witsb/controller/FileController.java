package be.mathiasbosman.witsb.controller;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.fs.core.util.FileServiceUtils;
import be.mathiasbosman.witsb.domain.File;
import be.mathiasbosman.witsb.domain.FileRecord;
import be.mathiasbosman.witsb.domain.UnlockNotification;
import be.mathiasbosman.witsb.service.NotificationService;
import be.mathiasbosman.witsb.service.PersistServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class FileController {

  private final PersistServiceImpl persistService;
  private final NotificationService notificationService;
  private final FileService fileService;

  @PostMapping(value = "/{context}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public FileRecord upload(@PathVariable String context,
      @RequestParam("file") MultipartFile multipartFile)
      throws IOException {
    return FileRecord.fromEntity(
        persistService.upload(context, multipartFile.getName(), multipartFile.getInputStream()));
  }

  @PostMapping(value = "/lock", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<FileRecord> lock(@RequestParam(name = "lockedGroupId") UUID lockedGroupId,
      @RequestParam("file") MultipartFile multipartFile) throws IOException {
    FileRecord record = FileRecord.fromEntity(
        persistService.uploadAndLock(lockedGroupId, multipartFile.getInputStream()));
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(record);
  }

  @PostMapping("/unlock/{lockGroupId}")
  public void unlock(@PathVariable UUID lockGroupId) {
    List<File> files = persistService.unlock(lockGroupId);
    var notification = new UnlockNotification(files.stream().map(FileRecord::fromEntity).toList());
    notificationService.notify(notification);
  }

  @PutMapping(value = "/{reference}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public FileRecord update(@PathVariable UUID reference,
      @RequestParam("file") MultipartFile multipartFile)
      throws IOException {
    return FileRecord.fromEntity(
        persistService.updateFile(reference, multipartFile.getInputStream()));
  }

  @GetMapping("/{reference}")
  public void download(@PathVariable UUID reference,
      @RequestParam(name = "version", required = false) Integer version,
      HttpServletResponse response) {

    Optional<File> file = version != null
        ? persistService.findFile(reference, version)
        : persistService.findFile(reference);

    file.ifPresentOrElse(f -> {
      if (!f.isLocked()) {
        writeFileStream(f, response);
      } else {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
      }
    }, () -> {
      log.error("No file found for {}(v.{})", reference, version);
      response.setStatus(HttpStatus.NOT_FOUND.value());
    });

  }

  @DeleteMapping("/{reference}")
  public void delete(@PathVariable UUID reference) {
    persistService.deleteFile(reference);
  }

  @GetMapping("/group/{groupId}")
  public List<FileRecord> listGroup(@PathVariable UUID groupId) {
    return persistService.getAllVersions(groupId).stream()
        .map(FileRecord::fromEntity)
        .toList();
  }

  private void writeFileStream(File file, HttpServletResponse response) {
    try {
      ContentDisposition disposition = ContentDisposition.attachment().filename(file.getFilename())
          .build();
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
      response.setHeader(HttpHeaders.CONTENT_TYPE,
          FileServiceUtils.getContentType(file.getFilename()));
      InputStream inputStream = fileService.open(persistService.toPath(file));
      IOUtils.copy(inputStream, response.getOutputStream());
    } catch (IOException e) {
      log.error("Error writing to output stream for {}", file.getReference(), e);
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }
}
