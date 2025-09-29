package be.mathiasbosman.witsb.controller;

import be.mathiasbosman.witsb.domain.EventType;
import be.mathiasbosman.witsb.service.sse.ServerEvent;
import java.util.UUID;

public record UnlockEvent(UUID groupUuid) implements ServerEvent<EventType> {

  @Override
  public Object payload() {
    return groupUuid;
  }

  @Override
  public EventType eventType() {
    return EventType.UNLOCKED;
  }
}
