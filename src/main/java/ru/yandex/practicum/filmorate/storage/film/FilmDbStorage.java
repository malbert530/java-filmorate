package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.mapper.FilmExtractor;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_BY_ID_QUERY = "SELECT f.*, r.name rating_name, g.genre_name, g.genre_id " +
            "FROM films f " +
            "JOIN rating r ON f.rating_id = r.id " +
            "LEFT JOIN (SELECT fg.*, g.name genre_name FROM film_genre fg " +
            "JOIN genres g ON fg.genre_id = g.id) g ON g.film_id = f.id WHERE f.id = ?";
    private static final String FIND_ALL_QUERY = "SELECT f.*, r.name rating_name, g.genre_name, g.genre_id " +
            "FROM films f " +
            "JOIN rating r ON f.rating_id = r.id " +
            "LEFT JOIN (SELECT fg.*, g.name genre_name FROM film_genre fg " +
            "JOIN genres g ON fg.genre_id = g.id) g ON g.film_id = f.id";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films " +
            "SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String INSERT_LIKE = "INSERT INTO film_user_like (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_user_like WHERE film_id = ? AND user_id = ?";

    private static final String FIND_MOST_POPULAR = "SELECT f.*, r.name rating_name, g.genre_name, g.genre_id, likes.like_count " +
            "FROM films f JOIN rating r ON f.rating_id = r.id " +
            "LEFT JOIN (SELECT fg.*, g.name genre_name FROM film_genre fg " +
            "JOIN genres g ON fg.genre_id = g.id) g ON g.film_id = f.id " +
            "RIGHT JOIN (SELECT f.id, l.like_count FROM films f " +
            "LEFT JOIN (SELECT film_id, COUNT(user_id) AS like_count " +
            "FROM film_user_like GROUP BY film_id) AS l ON f.id = l.film_id " +
            "ORDER BY l.like_count DESC LIMIT ?) likes ON likes.id = f.id";


    private final JdbcTemplate jdbc;
    private final FilmExtractor filmExtractor;

    @Override
    public List<Film> findAll() {
        return jdbc.query(FIND_ALL_QUERY, filmExtractor);
    }

    @Override
    public Film create(Film film) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getRating().getId());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        TreeSet<Genre> genre = film.getGenre();
        if (genre != null) {
            for (Genre g : genre) {
                int update = jdbc.update(INSERT_FILM_GENRE, id, g.getId());
                String message = update == 1 ? "Жанры удалось обновить" : "Жанры не удалось обновить";
                log.info(message);
            }
        }

        if (id != null) {
            film.setId(id);
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        int update1 = jdbc.update(UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getRating().getId(),
                newFilm.getId());
        if (update1 == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }

        TreeSet<Genre> genre = newFilm.getGenre();
        jdbc.update(DELETE_FILM_GENRES, newFilm.getId());
        if (genre != null) {
            for (Genre g : genre) {
                int update = jdbc.update(INSERT_FILM_GENRE, newFilm.getId(), g.getId());
                String message = update == 1 ? "Жанры удалось обновить" : "Жанры не удалось обновить";
                log.info(message);
            }
        }
        return newFilm;
    }

    @Override
    public void putLike(Long id, Long userId) {
        jdbc.update(INSERT_LIKE, id, userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        jdbc.update(DELETE_LIKE, id, userId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        return jdbc.query(FIND_MOST_POPULAR, filmExtractor, count);
    }

    @Override
    public Film getFilmById(Long id) {
        List<Film> list = jdbc.query(FIND_BY_ID_QUERY, filmExtractor, id);
        if (list.isEmpty()) {
            String errorMessage = String.format("Фильм с id %d не найден", id);
            log.warn(errorMessage);
            throw new FilmNotFoundException(errorMessage);
        }
        return list.getFirst();
    }
}
