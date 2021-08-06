package be.mathiasbosman.witsb;

import be.mathiasbosman.witsb.entity.Item;
import be.mathiasbosman.witsb.service.ItemService;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
@Configuration
public class CronConfig {

  private final ItemService itemService;

  @Scheduled(fixedDelay = 60000)
  public void cleanupItems() {
    log.info("Cleaning up items");
    List<Item> items = itemService.autoDelete(1, ChronoUnit.DAYS);
    log.info("Cleaned up {} items", items.size());
  }
}
