package be.mathiasbosman.witsb.controller;

import be.mathiasbosman.witsb.domain.File;
import be.mathiasbosman.witsb.domain.FileRecord;
import be.mathiasbosman.witsb.domain.UnlockNotification;
import be.mathiasbosman.witsb.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Profile("local")
@RestController
@RequestMapping("test")
@RequiredArgsConstructor
public class TestController {

  private final NotificationService notificationService;


  @PostMapping("/notification")
  public void testNotification() {
    UUID id = UUID.randomUUID();
    log.debug("Sending new unlock notification {}", id);
    notificationService.notify(new UnlockNotification(List.of(
        FileRecord.fromEntity(File.builder().id(id).build())
    )));
  }

}
