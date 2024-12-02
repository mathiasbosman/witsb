package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.WebSocketConfig;
import be.mathiasbosman.witsb.domain.UnlockNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  static final String TOPIC_UNLOCKED = "/unlocked";

  private final SimpMessagingTemplate wsTemplate;

  @Override
  public void notify(UnlockNotification unlockNotification) {
    sendToTopic(TOPIC_UNLOCKED, unlockNotification);
  }

  private void sendToTopic(String topic, Object payload) {
    if (payload == null) {
      return;
    }

    wsTemplate.convertAndSend(WebSocketConfig.WS_TOPIC + topic, payload);
  }
}
