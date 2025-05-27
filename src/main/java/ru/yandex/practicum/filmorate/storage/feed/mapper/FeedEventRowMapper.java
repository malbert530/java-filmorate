package ru.yandex.practicum.filmorate.storage.feed.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class FeedEventRowMapper implements RowMapper<FeedEvent> {

    @Override
    public FeedEvent mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        FeedEvent feedEvent = new FeedEvent();
        feedEvent.setEventId(resultSet.getLong("id"));
        feedEvent.setTimestamp(resultSet.getTimestamp("timestamp"));
        feedEvent.setUserId(resultSet.getLong("user_id"));
        feedEvent.setEntityId(resultSet.getLong("entity_id"));

        EventType eventType = new EventType(resultSet.getInt("eventType_id"),
                resultSet.getString("eventType_name"));
        feedEvent.setEventType(eventType);

        Operation operation = new Operation(resultSet.getInt("operation_id"),
                resultSet.getString("operation_name"));
        feedEvent.setOperation(operation);

        return feedEvent;
    }
}
