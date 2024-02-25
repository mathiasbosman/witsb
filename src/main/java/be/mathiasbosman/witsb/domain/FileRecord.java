package be.mathiasbosman.witsb.domain;

import java.util.UUID;

public record FileRecord(String fileName, String context, UUID reference, int version) {

}
