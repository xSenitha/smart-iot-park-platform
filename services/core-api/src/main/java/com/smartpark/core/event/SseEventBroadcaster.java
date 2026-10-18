package com.smartpark.core.event;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEventBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(SseEventBroadcaster.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(180_000L); // 3 minutes timeout

        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitter);
        });
        emitter.onError(e -> {
            emitter.complete();
            emitters.remove(emitter);
        });

        try {
            emitter.send(SseEmitter.event()
                .name("CONNECT")
                .data("Connected to Smart IoT Park Platform Event Stream"));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE ping", e);
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcast(String eventName, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}
