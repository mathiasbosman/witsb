package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.configuration.WebSocketConfigurer;
import be.mathiasbosman.witsb.domain.WebSocketMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

  private final SimpMessagingTemplate wsTemplate;

  @Override
  public void sendMessage(@NonNull WebSocketMessage message) {
    wsTemplate.convertAndSend(WebSocketConfigurer.WS_TOPIC + message.topic(), message.payload());
  }
}
