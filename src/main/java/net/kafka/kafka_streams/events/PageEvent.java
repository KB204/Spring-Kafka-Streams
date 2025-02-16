package net.kafka.kafka_streams.events;

import java.time.LocalDateTime;

public record PageEvent(String name, String user, LocalDateTime date, long duration) {}
