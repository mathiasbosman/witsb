package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.domain.UnlockNotification;

public interface NotificationService {

  void notify(UnlockNotification unlockNotification);
}
