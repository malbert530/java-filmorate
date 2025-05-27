package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FeedEventDto;
import ru.yandex.practicum.filmorate.model.FeedEvent;

public class FeedEventMapper {
    public static FeedEventDto convertToDto(FeedEvent feedEvent) {
        return FeedEventDto.builder()
                .id(feedEvent.getId())
                .userId(feedEvent.getUserId())
                .operation(feedEvent.getOperation().getName())
                .eventType(feedEvent.getEventType().getName())
                .entityId(feedEvent.getEntityId())
                .timestamp(feedEvent.getTimestamp().toInstant().toEpochMilli())
                .build();
    }
}
