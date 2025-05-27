package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EventTypeNotFoundException;
import ru.yandex.practicum.filmorate.storage.feed.mapper.EventTypeMapExtractor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventTypeDbStorage {
    private static final String FIND_BY_ID = "SELECT name FROM eventTypes WHERE id = ?";
    private static final String GET_ALL = "SELECT name, id FROM eventTypes";

    private final JdbcTemplate jdbc;
    private final EventTypeMapExtractor extractor;

    public String getNameById(Integer id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID, String.class, id);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = String.format("EventType с id %d не найден", id);
            log.warn(errorMessage);
            throw new EventTypeNotFoundException(errorMessage);
        }
    }

    public Map<String, Integer> getMap() {
        return jdbc.query(GET_ALL, extractor);
    }
}
