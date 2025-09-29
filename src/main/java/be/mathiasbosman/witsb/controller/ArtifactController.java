package be.mathiasbosman.witsb.controller;

import be.mathiasbosman.fs.core.service.FileService;
import be.mathiasbosman.fs.core.util.FileServiceUtils;
import be.mathiasbosman.witsb.domain.Artifact;
import be.mathiasbosman.witsb.domain.ArtifactDto;
import be.mathiasbosman.witsb.service.ArtifactServiceImpl;
import be.mathiasbosman.witsb.service.sse.ServerEventChannel;
import be.mathiasbosman.witsb.service.sse.SseHub;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class ArtifactController {

  static final String WS_TOPIC_UNLOCKED = "/unlocked";

  private final ArtifactServiceImpl artifactService;
  private final SseHub sseHub;
  private final FileService fileService;

  @GetMapping(value = "/register/{uuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter register(@PathVariable UUID uuid) {
    return sseHub.subscribe(uuid).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid subscription request"));
  }

  @PostMapping(value = "/{context}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ArtifactDto upload(@PathVariable String context,
      @RequestParam("file") MultipartFile multipartFile)
      throws IOException {
    return ArtifactDto.fromEntity(
        artifactService.upload(context, multipartFile.getName(), multipartFile.getInputStream()));
  }

  @PostMapping(value = "/lock", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ArtifactDto> lock(@RequestParam(name = "lockedGroupId") UUID lockedGroupId,
      @RequestParam("file") MultipartFile multipartFile) throws IOException {
    ArtifactDto record = ArtifactDto.fromEntity(
        artifactService.uploadAndLock(lockedGroupId, multipartFile.getInputStream()));
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(record);
  }

  @PostMapping("/unlock/{lockGroupId}")
  public void unlock(@PathVariable UUID lockGroupId) {
    artifactService.unlock(lockGroupId);
    sseHub.publish(new ServerEventChannel(lockGroupId), new UnlockEvent(lockGroupId));
  }

  @PutMapping(value = "/{reference}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ArtifactDto update(@PathVariable UUID reference,
      @RequestParam("file") MultipartFile multipartFile)
      throws IOException {
    return ArtifactDto.fromEntity(
        artifactService.updateFile(reference, multipartFile.getInputStream()));
  }

  /**
   * Download a file by reference and optional reference. The {@link HttpServletResponse} status
   * gets adjusted accordingly:
   * <ul>
   *   <li>{@link HttpStatus#OK} if the file is found and not locked</li>
   *   <li>{@link HttpStatus#UNAUTHORIZED} if the file is found but locked</li>
   *   <li>{@link HttpStatus#NOT_FOUND} if the file is not found</li>
   * </ul>
   *
   * @param reference the file reference
   * @param version   (optional) the version
   * @param response  the response to adjust
   */
  @GetMapping("/{reference}")
  public void download(@PathVariable UUID reference,
      @RequestParam(name = "version", required = false) Integer version,
      HttpServletResponse response) {

    Optional<Artifact> file = version != null
        ? artifactService.findFile(reference, version)
        : artifactService.findFile(reference);

    file.ifPresentOrElse(f -> {
      if (!f.isLocked()) {
        writeFileStream(f, response);
      } else {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
      }
    }, () -> {
      log.error("No file found for [{}] (v.{})", reference, version);
      response.setStatus(HttpStatus.NOT_FOUND.value());
    });

  }

  @DeleteMapping("/{reference}")
  public void delete(@PathVariable UUID reference) {
    artifactService.deleteFile(reference);
  }

  @GetMapping("/group/{groupId}")
  public List<ArtifactDto> listGroup(@PathVariable UUID groupId) {
    return artifactService.getAllVersions(groupId).stream()
        .map(ArtifactDto::fromEntity)
        .toList();
  }

  private void writeFileStream(Artifact artifact, HttpServletResponse response) {
    try {
      ContentDisposition disposition = ContentDisposition.attachment()
          .filename(artifact.getFilename())
          .build();
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
      response.setHeader(HttpHeaders.CONTENT_TYPE,
          FileServiceUtils.getContentType(artifact.getFilename()));
      InputStream inputStream = fileService.open(artifactService.toPath(artifact));
      IOUtils.copy(inputStream, response.getOutputStream());
    } catch (IOException e) {
      log.error("Error writing to output stream for [{}]", artifact.getReference(), e);
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }
}
