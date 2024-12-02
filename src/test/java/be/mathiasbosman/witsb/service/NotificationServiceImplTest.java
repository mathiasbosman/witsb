package be.mathiasbosman.witsb.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import be.mathiasbosman.witsb.WebSocketConfig;
import be.mathiasbosman.witsb.domain.FileMother;
import be.mathiasbosman.witsb.domain.FileRecord;
import be.mathiasbosman.witsb.domain.UnlockNotification;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock
  private SimpMessagingTemplate wsTemplate;

  @InjectMocks
  private NotificationServiceImpl notificationService;

  @Test
  void notify_sendsMessageToTopic() {
    UnlockNotification unlockNotification = new UnlockNotification(List.of(
        FileRecord.fromEntity(FileMother.random()),
        FileRecord.fromEntity(FileMother.random())
    ));

    notificationService.notify(unlockNotification);

    verify(wsTemplate).convertAndSend(
        eq(WebSocketConfig.WS_TOPIC + NotificationServiceImpl.TOPIC_UNLOCKED),
        eq(unlockNotification));
  }

  @Test
  void notify_doesNotSendMessageWhenPayloadIsNull() {
    notificationService.notify(null);

    verifyNoInteractions(wsTemplate);
  }
}