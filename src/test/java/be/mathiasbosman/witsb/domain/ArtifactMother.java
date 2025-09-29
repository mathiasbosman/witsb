package be.mathiasbosman.witsb.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ArtifactMother {

  public static Artifact random() {
    Artifact f = new Artifact();
    f.setReference(UUID.randomUUID());
    f.setContext("foo");
    f.setGroupId(UUID.randomUUID());
    f.setVersion(0);
    f.setFilename("bar");
    f.setUploadedOn(LocalDateTime.now());
    return f;
  }

  public static Artifact withVersion(UUID groupId, int version) {
    Artifact f = random();
    f.setGroupId(groupId);
    f.setVersion(version);
    return f;
  }

  public static Artifact withUploadedOn(LocalDateTime uploadedOn) {
    Artifact f = random();
    f.setUploadedOn(uploadedOn);
    return f;
  }

  public static Artifact withLocked(String fileName, boolean locked) {
    Artifact f = random();
    f.setFilename(fileName);
    f.setLocked(locked);
    return f;
  }

}
