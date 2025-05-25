package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.mapper.FilmExtractor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_BY_ID_QUERY = "SELECT f.*, r.name rating_name, g.genre_name, g.genre_id, " +
            "directors.director_id, directors.director_name " +
            "FROM films f " +
            "JOIN rating r ON f.rating_id = r.id " +
            "LEFT JOIN (SELECT fg.*, g.name genre_name FROM film_genre fg " +
            "JOIN genres g ON fg.genre_id = g.id) g ON g.film_id = f.id " +
            "LEFT JOIN (SELECT fd.*, d.name director_name FROM film_director fd " +
            "JOIN directors d ON fd.director_id = d.id) directors ON directors.film_id = f.id " +
            "WHERE f.id = ?";
    private static final String FIND_ALL_QUERY = "SELECT f.*, r.name rating_name, g.genre_name, g.genre_id, " +
            "directors.director_id, directors.director_name " +
            "FROM films f " +
            "JOIN rating r ON f.rating_id = r.id " +
            "LEFT JOIN (SELECT fg.*, g.name genre_name FROM film_genre fg " +
            "JOIN genres g ON fg.genre_id = g.id) g ON g.film_id = f.id " +
            "LEFT JOIN (SELECT fd.*, d.name director_name FROM film_director fd " +
            "JOIN directors d ON fd.director_id = d.id) directors ON directors.film_id = f.id";

    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_FILM_DIRECTOR = "INSERT INTO film_director(film_id, director_id) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films " +
            "SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE id = ?";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String DELETE_FILM_DIRECTORS = "DELETE FROM film_director WHERE film_id = ?";
    private static final String INSERT_LIKE = "MERGE INTO film_user_like KEY(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_user_like WHERE film_id = ? AND user_id = ?";

    private static final String FIND_MOST_POPULAR = "SELECT f.*, r.name rating_name, g.genre_name, g.genre_id, " +
            "directors.director_id, directors.director_name, likes.like_count " +
            "FROM films f JOIN rating r ON f.rating_id = r.id " +
            "LEFT JOIN (SELECT fg.*, g.name genre_name FROM film_genre fg " +
            "JOIN genres g ON fg.genre_id = g.id) g ON g.film_id = f.id " +
            "RIGHT JOIN (SELECT f.id, l.like_count FROM films f " +
            "LEFT JOIN (SELECT film_id, COUNT(user_id) AS like_count " +
            "FROM film_user_like GROUP BY film_id) AS l ON f.id = l.film_id " +
            "ORDER BY l.like_count DESC LIMIT ?) likes ON likes.id = f.id " +
            "LEFT JOIN (SELECT fd.*, d.name director_name FROM film_director fd " +
            "JOIN directors d ON fd.director_id = d.id) directors ON directors.film_id = f.id";
    private static final String FIND_DIRECTOR_FILMS_SORTED_BY_YEAR = "SELECT f.*, r.name rating_name, g.genre_name, g.genre_id, " +
            "directors.director_id, directors.director_name " +
            "FROM films f " +
            "JOIN rating r ON f.rating_id = r.id " +
            "LEFT JOIN (SELECT fg.*, g.name genre_name FROM film_genre fg " +
            "JOIN genres g ON fg.genre_id = g.id) g ON g.film_id = f.id " +
            "RIGHT JOIN (SELECT fd.*, d.name director_name FROM film_director fd " +
            "JOIN directors d ON fd.director_id = d.id WHERE d.id = ?) directors ON directors.film_id = f.id " +
            "ORDER BY f.release_date";
    private static final String FIND_DIRECTOR_FILMS_SORTED_BY_LIKES = "SELECT f.*, r.name rating_name, g.genre_name, " +
            "g.genre_id, directors.director_id, directors.director_name " +
            "FROM films f " +
            "JOIN rating r ON f.rating_id = r.id " +
            "LEFT JOIN (SELECT fg.*, g.name genre_name " +
            "FROM film_genre fg " +
            "JOIN genres g ON fg.genre_id = g.id) g ON g.film_id = f.id " +
            "RIGHT JOIN (SELECT f.id, l.like_count " +
            "FROM films f " +
            "LEFT JOIN (SELECT film_id, COUNT(user_id) AS like_count " +
            "FROM film_user_like " +
            "GROUP BY film_id) AS l ON f.id = l.film_id " +
            "ORDER BY l.like_count DESC) likes ON likes.id = f.id " +
            "JOIN (SELECT fd.*, d.name director_name FROM film_director fd " +
            "JOIN directors d ON fd.director_id = d.id WHERE d.id = ?) directors ON directors.film_id = f.id";

    private static final String FIND_COMMON = "SELECT f.*, r.name AS rating_name, g.id AS genre_id, g.name AS genre_name " +
            "FROM films f " +
            "JOIN rating r ON f.rating_id = r.id " +
            "LEFT JOIN film_genre fg ON f.id = fg.film_id " +
            "LEFT JOIN genres g ON fg.genre_id = g.id " +
            "WHERE f.id IN (" +
            "  SELECT ful1.film_id FROM film_user_like ful1 " +
            "  WHERE ful1.user_id = ? " +
            "  INTERSECT " +
            "  SELECT ful2.film_id FROM film_user_like ful2 " +
            "  WHERE ful2.user_id = ?" +
            ") " +
            "ORDER BY (SELECT COUNT(*) FROM film_user_like WHERE film_id = f.id) DESC";

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

        if (film.getGenre() != null) {
            List<Integer> genreIds = film.getGenre().stream().map(Genre::getId).toList();
            batchInsertGenre(id, genreIds);
        }

        if (film.getDirectors() != null) {
            List<Long> directorIds = film.getDirectors().stream().map(Director::getId).toList();
            batchInsertDirector(id, directorIds);
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

        jdbc.update(DELETE_FILM_GENRES, newFilm.getId());
        if (newFilm.getGenre() != null) {
            List<Integer> genreIds = newFilm.getGenre().stream().map(Genre::getId).toList();
            batchInsertGenre(newFilm.getId(), genreIds);
        }

        jdbc.update(DELETE_FILM_DIRECTORS, newFilm.getId());
        if (newFilm.getDirectors() != null) {
            List<Long> directorIds = newFilm.getDirectors().stream().map(Director::getId).toList();
            batchInsertDirector(newFilm.getId(), directorIds);
        }

        return newFilm;
    }

    @Override
    public void deleteById(Long id) {
        int rowsDeleted = jdbc.update(DELETE_FILM_QUERY, id);
        if (rowsDeleted == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }
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

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return jdbc.query(FIND_COMMON, filmExtractor, userId, friendId);
    }

    @Override
    public List<Film> getFilmsByIds(Collection<Long> ids) {

        String idsString = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = "SELECT f.*, r.name AS rating_name, g.id AS genre_id, g.name AS genre_name " +
                "FROM films f " +
                "JOIN rating r ON f.rating_id = r.id " +
                "LEFT JOIN film_genre fg ON f.id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.id " +
                "WHERE f.id IN (" + idsString + ")";

        return jdbc.query(sql, filmExtractor);
    }

    public List<Film> getFilmByDirectorIdSortedByYear(Long id) {
        return jdbc.query(FIND_DIRECTOR_FILMS_SORTED_BY_YEAR, filmExtractor, id);
    }

    @Override
    public List<Film> getFilmByDirectorIdSortedByLikes(Long id) {
        return jdbc.query(FIND_DIRECTOR_FILMS_SORTED_BY_LIKES, filmExtractor, id);
    }

    private void batchInsertDirector(Long filmId, List<Long> directorIds) {
        jdbc.batchUpdate(INSERT_FILM_DIRECTOR, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, directorIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return directorIds.size();
            }
        });
    }

    private void batchInsertGenre(Long filmId, List<Integer> genreIds) {
        jdbc.batchUpdate(INSERT_FILM_GENRE, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setInt(2, genreIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return genreIds.size();
            }
        });
    }
}
