package ru.yandex.practicum.filmorate.storage.rating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.RatingNotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.rating.mapper.RatingRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingDbStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM rating";
    private static final String FIND_BY_ID = "SELECT * FROM rating WHERE id = ?";

    private final JdbcTemplate jdbc;
    private final RatingRowMapper mapper;

    public Collection<Rating> getAll() {
        List<Rating> ratings = jdbc.query(FIND_ALL_QUERY, mapper);
        return new TreeSet<>(ratings);
    }

    public Rating getRatingById(Integer id) {
        Rating rating = null;
        try {
            if (id != null) {
                rating = jdbc.queryForObject(FIND_BY_ID, mapper, id);
            }
            return rating;
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = String.format("Рейтинг с id %d не найден", id);
            log.warn(errorMessage);
            throw new RatingNotFoundException(errorMessage);
        }
    }
}
