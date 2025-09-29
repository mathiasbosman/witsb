package be.mathiasbosman.witsb.service.sse;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseHub implements ApplicationListener<ContextClosedEvent> {

  /**
   * Active SSE subscribers per channel.
   */
  private final Map<ServerEventChannel, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();


  /**
   * Registers (subscribes) an {@link SseEmitter} to a {@link ServerEventChannel}.
   *
   * @param serverEventChannel the logical event channel
   * @param emitter            the emitter to receive events for the given channel
   */
  public void register(ServerEventChannel serverEventChannel, SseEmitter emitter) {
    log.debug("Registering SseEmitter for channel {}", serverEventChannel);
    emitters.computeIfAbsent(serverEventChannel,
            k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
        .add(emitter);
  }

  /**
   * Unregisters (unsubscribes) an {@link SseEmitter} from a {@link ServerEventChannel}.
   * <p>If the channel becomes empty after removal, the channel is removed from the internal
   * map.</p>
   *
   * @param serverEventChannel the channel to unregister from
   * @param emitter            the emitter to remove
   */
  public void unregister(ServerEventChannel serverEventChannel, SseEmitter emitter) {
    log.debug("Unregistering SseEmitter for channel {}", serverEventChannel);
    Optional.ofNullable(emitters.get(serverEventChannel)).ifPresent(set -> {
      set.remove(emitter);
      if (set.isEmpty()) {
        emitters.remove(serverEventChannel);
      }
    });
  }

  public void publish(ServerEventChannel serverEventChannel, ServerEvent<?> event) {
    String id = serverEventChannel + " " + UUID.randomUUID();
    log.debug("Publishing SseEmitter for channel {} with id [{}]", serverEventChannel, id);
    Set<SseEmitter> subs = emitters.getOrDefault(serverEventChannel, Set.of());
    subs.forEach(emitter -> {
      try {
        log.trace("Sending message to channel {} with id [{}] and payload [{}]", serverEventChannel,
            id,
            event.getPayload());
        send(emitter, serverEventChannel, event);
      } catch (Exception e) {
        // drop broken connection
        log.debug("Sending message failed [{}], will unregister", e.getMessage());
        unregister(serverEventChannel, emitter);
        try {
          emitter.complete();
        } catch (Exception ignored) {
        }
        log.warn("Broken SSE connection for channel {} and id {}", serverEventChannel, id);
      }
    });
  }

  private void send(SseEmitter emitter, ServerEventChannel serverEventChannel, ServerEvent<?> event)
      throws IOException {
    emitter.send(SseEmitter.event()
        .id(serverEventChannel + "-" + UUID.randomUUID())
        .data(event.getPayload())
    );
  }

  public Optional<SseEmitter> subscribe(@NonNull UUID uuid) {

    SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(5));
    ServerEventChannel serverEventChannel = new ServerEventChannel(uuid);
    register(serverEventChannel, emitter);

    Runnable cleanup = () -> unregister(serverEventChannel, emitter);
    emitter.onCompletion(cleanup);
    emitter.onTimeout(cleanup);
    emitter.onError(e -> cleanup.run());

    return Optional.of(emitter);
  }

  @Override
  public void onApplicationEvent(@NonNull ContextClosedEvent ignored) {
    emitters.values().forEach(set -> set.forEach(ResponseBodyEmitter::complete));
  }
}
