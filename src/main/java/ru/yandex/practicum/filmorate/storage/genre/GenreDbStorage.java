package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDbStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_GENRES_BY_FILM_ID = "SELECT g.id, g.name FROM film_genre f " +
            "JOIN genres g ON f.genre_id = g.id WHERE f.film_id = ? ORDER BY g.id";

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    public Collection<Genre> getAll() {
        List<Genre> genres = jdbc.query(FIND_ALL_QUERY, mapper);
        return new TreeSet<>(genres);
    }

    public Genre getGenreById(Integer id) {
        try {
            Genre genreById = jdbc.queryForObject(FIND_BY_ID, mapper, id);
            return genreById;
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = String.format("Рейтинг с id %d не найден", id);
            log.warn(errorMessage);
            throw new GenreNotFoundException(errorMessage);
        }
    }

    public TreeSet<Genre> getGenresByFilmId(Long id) {
        List<Genre> genreList = jdbc.query(FIND_GENRES_BY_FILM_ID, mapper, id);
        return new TreeSet<>(genreList);
    }
}
