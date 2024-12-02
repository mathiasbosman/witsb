package be.mathiasbosman.witsb.domain;

import java.util.List;

public record UnlockNotification(List<FileRecord> unlockedFiles) {

}
