package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.domain.UnlockNotification;

public interface NotificationService {

  /**
   * Notify an unlock event.
   *
   * @param unlockNotification the notification
   */
  void notify(UnlockNotification unlockNotification);
}
