package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.OperationNotFoundException;
import ru.yandex.practicum.filmorate.storage.feed.mapper.OperationMapExtractor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationDbStorage {
    private static final String FIND_BY_ID = "SELECT name FROM operations WHERE id = ?";
    private static final String GET_ALL = "SELECT name, id FROM operations";

    private final JdbcTemplate jdbc;
    private final OperationMapExtractor extractor;

    public String getNameById(Integer id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID, String.class, id);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = String.format("Операция с id %d не найдена", id);
            log.warn(errorMessage);
            throw new OperationNotFoundException(errorMessage);
        }
    }

    public Map<String, Integer> getMap() {
        return jdbc.query(GET_ALL, extractor);
    }
}
