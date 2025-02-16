package net.kafka.kafka_streams.controller;

import net.kafka.kafka_streams.events.PageEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class PageEventController {
    private final StreamBridge bridge;

    public PageEventController(StreamBridge bridge) {
        this.bridge = bridge;
    }

    @GetMapping("/publish")
    PageEvent sendEvent(@RequestParam String name,@RequestParam String topic) {
        PageEvent event = new PageEvent(name, Math.random()>0.5?"U1":"U2", LocalDateTime.now(),10+new Random().nextInt(10000));
        bridge.send(topic,event);
        return event;
    }
}
