package com.smartpark.core.event.controller;

import com.smartpark.core.event.SseEventBroadcaster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event Stream", description = "Server-Sent Events (SSE) for real-time frontend streaming")
public class EventStreamController {

    private final SseEventBroadcaster broadcaster;

    public EventStreamController(SseEventBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to live platform events (alerts, status changes, predictions)")
    public SseEmitter subscribe() {
        return broadcaster.createEmitter();
    }
}
