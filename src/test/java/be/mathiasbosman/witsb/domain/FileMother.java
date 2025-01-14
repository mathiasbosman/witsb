package be.mathiasbosman.witsb.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileMother {

  public static File random() {
    File f = new File();
    f.setReference(UUID.randomUUID());
    f.setContext("foo");
    f.setGroupId(UUID.randomUUID());
    f.setVersion(0);
    f.setFilename("bar");
    f.setUploadedOn(LocalDateTime.now());
    return f;
  }

  public static File withVersion(UUID groupId, int version) {
    File f = random();
    f.setGroupId(groupId);
    f.setVersion(version);
    return f;
  }

  public static File withUploadedOn(LocalDateTime uploadedOn) {
    File f = random();
    f.setUploadedOn(uploadedOn);
    return f;
  }

  public static File withLocked(String fileName, boolean locked) {
    File f = random();
    f.setFilename(fileName);
    f.setLocked(locked);
    return f;
  }

}
