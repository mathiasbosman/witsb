package be.mathiasbosman.witsb.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import be.mathiasbosman.witsb.configuration.WebSocketConfigurer;
import be.mathiasbosman.witsb.domain.FileMother;
import be.mathiasbosman.witsb.domain.FileRecord;
import be.mathiasbosman.witsb.domain.WebSocketMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceImplTest {

  @Mock
  private SimpMessagingTemplate wsTemplate;

  @InjectMocks
  private WebSocketServiceImpl notificationService;

  @Test
  void notify_sendsMessageToTopic() {
    WebSocketMessage unlockNotification = new WebSocketMessage("/topicX", List.of(
        FileRecord.fromEntity(FileMother.random()),
        FileRecord.fromEntity(FileMother.random())
    ));

    notificationService.sendMessage(unlockNotification);

    verify(wsTemplate).convertAndSend(
        eq(WebSocketConfigurer.WS_TOPIC + "/topicX"),
        eq(unlockNotification.payload()));
  }
}