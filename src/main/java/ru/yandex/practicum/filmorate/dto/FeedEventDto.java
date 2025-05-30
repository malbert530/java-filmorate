package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class FeedEventDto {
    private Long eventId;
    private long timestamp;
    private Long userId;
    private String eventType;
    private String operation;
    private Long entityId;
}
