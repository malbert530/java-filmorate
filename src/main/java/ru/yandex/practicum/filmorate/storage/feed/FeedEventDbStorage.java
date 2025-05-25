package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.feed.mapper.FeedEventRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class FeedEventDbStorage implements FeedEventStorage {
    private static final String FIND_ALL_QUERY = "SELECT fe.id, fe.timestamp, fe.user_id, fe.eventType_id, " +
            "et.name AS eventType_name, fe.operation_id, ot.name AS operation_name, fe.entity_id " +
            "FROM feed_events AS fe " +
            "LEFT OUTER JOIN eventTypes AS et ON fe.eventType_id = et.id " +
            "LEFT OUTER JOIN operations AS ot ON fe.operation_id = ot.id " +
            "WHERE fe.user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO feed_events(timestamp, user_id, eventType_id, operation_id, entity_id)" +
            "VALUES (?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbc;
    private final FeedEventRowMapper rowMapper;

    @Override
    public List<FeedEvent> getFeed(Long userId) {
        return jdbc.query(FIND_ALL_QUERY, rowMapper, userId);
    }

    @Override
    public FeedEvent addToFeed(FeedEvent feedEvent) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, feedEvent.getTimestamp());
            ps.setLong(2, feedEvent.getUserId());
            ps.setInt(3, feedEvent.getEventType().getId());
            ps.setInt(4, feedEvent.getOperation().getId());
            ps.setLong(5,feedEvent.getEntityId());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            feedEvent.setId(id);
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }

        return feedEvent;
    }
}
