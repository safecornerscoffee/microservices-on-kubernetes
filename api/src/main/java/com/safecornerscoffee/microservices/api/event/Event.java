package com.safecornerscoffee.microservices.api.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class Event<K, T> {

    public enum Type {CREATE, DELETE}

    private Event.Type type;
    private K key;
    private T data;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime eventCreatedAt;

    public Event() {
        this.type = null;
        this.key = null;
        this.data = null;
        this.eventCreatedAt = null;
    }

    public Event(Type type, K key, T data) {
        this.type = type;
        this.key = key;
        this.data = data;
        this.eventCreatedAt = LocalDateTime.now();
    }

    public Type getType() {
        return type;
    }

    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }
}
