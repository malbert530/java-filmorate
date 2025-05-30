package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedEvent {
    private Long eventId;
    private Timestamp timestamp;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long entityId;
}
