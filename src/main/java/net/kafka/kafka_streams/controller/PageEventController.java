package net.kafka.kafka_streams.controller;

import net.kafka.kafka_streams.events.PageEvent;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class PageEventController {
    private final StreamBridge bridge;
    private final InteractiveQueryService service;

    public PageEventController(StreamBridge bridge, InteractiveQueryService service) {
        this.bridge = bridge;
        this.service = service;
    }

    @GetMapping("/publish")
    PageEvent sendEvent(@RequestParam String name,@RequestParam String topic) {
        PageEvent event = new PageEvent(name, Math.random()>0.5?"U1":"U2", LocalDateTime.now(),10+new Random().nextInt(10000));
        bridge.send(topic,event);
        return event;
    }

    @GetMapping(value = "/analytics",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Map<String , Long>> analytics() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence->{
                    Map<String,Long> stringLongMap = new HashMap<>();
                    ReadOnlyWindowStore<String, Long> windowStore = service.getQueryableStore("count-store", QueryableStoreTypes.windowStore());
                    Instant now = Instant.now();
                    Instant from = now.minusMillis(5000);
                    try (KeyValueIterator<Windowed<String>, Long> fetchAll = windowStore.fetchAll(from, now)) {
                        while (fetchAll.hasNext()) {
                            KeyValue<Windowed<String>, Long> next = fetchAll.next();
                            stringLongMap.put(next.key.key(), next.value);
                        }
                    }
                    return stringLongMap;
                });
    }
}
