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
            "et.name AS eventType_name, fe.operation_id, ot.name AS operation_name, map.entity_id " +
            "FROM feed_events AS fe " +
            "LEFT OUTER JOIN eventTypes AS et ON fe.eventType_id = et.id " +
            "LEFT OUTER JOIN operations AS ot ON fe.operation_id = ot.id " +
            "JOIN (SELECT event_id, film_id AS entity_id FROM event_to_film " +
            "UNION ALL " +
            "SELECT event_id, user_id AS entity_id FROM event_to_user " +
            "UNION ALL " +
            "SELECT event_id, review_id AS entity_id FROM event_to_review) AS map ON fe.id = map.event_id " +
            "WHERE fe.user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO feed_events(timestamp, user_id, eventType_id, operation_id)" +
            "VALUES (?, ?, ?, ?)";
    private static final String INSERT_FILM_ENTITY_QUERY = "INSERT INTO event_to_film (event_id, film_id) VALUES (?, ?)";
    private static final String INSERT_USER_ENTITY_QUERY = "INSERT INTO event_to_user (event_id, user_id) VALUES (?, ?)";
    private static final String INSERT_REVIEW_ENTITY_QUERY = "INSERT INTO event_to_review (event_id, review_id) VALUES (?, ?)";

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

    @Override
    public void insertFilmEntityToFeed(Long eventId, Long filmId) {
        int rowsUpdated = jdbc.update(INSERT_FILM_ENTITY_QUERY, eventId, filmId);
        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось сохранить данные");
        }
    }

    @Override
    public void insertUserEntityToFeed(Long eventId, Long userId) {
        int rowsUpdated = jdbc.update(INSERT_USER_ENTITY_QUERY, eventId, userId);
        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось сохранить данные");
        }
    }

    @Override
    public void insertReviewEntityToFeed(Long eventId, Long reviewId) {
        int rowsUpdated = jdbc.update(INSERT_REVIEW_ENTITY_QUERY, eventId, reviewId);
        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось сохранить данные");
        }
    }
}
