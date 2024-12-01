package be.mathiasbosman.witsb.domain;

import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileMother {

  public static File of(UUID groupId, int version) {
    return File.builder()
        .context("foo")
        .groupId(groupId)
        .version(version)
        .filename("bar")
        .build();
  }

  public static File random() {
    return of(UUID.randomUUID(), 0);
  }

}
