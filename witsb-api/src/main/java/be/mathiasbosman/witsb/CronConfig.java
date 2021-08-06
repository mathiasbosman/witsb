package be.mathiasbosman.witsb;

import be.mathiasbosman.witsb.service.ItemService;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@AllArgsConstructor
public class CronConfig {

  private final ItemService itemService;

  @Scheduled(cron = "0 * * * * *")
  public void cleanupItems() {
    itemService.autoDelete(24, ChronoUnit.HOURS);
  }
}
