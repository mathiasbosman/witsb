package be.mathiasbosman.witsb.service;

import be.mathiasbosman.witsb.domain.WebSocketMessage;

public interface WebSocketService {

  /**
   * Send a {@link WebSocketMessage}.
   *
   * @param message the message to send
   */
  void sendMessage(WebSocketMessage message);
}
