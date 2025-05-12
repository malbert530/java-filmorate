package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films " +
            "SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = " SELECT * FROM films WHERE id = ?";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String FIND_MAX_GENRE_ID = "SELECT MAX(id) FROM genres";
    private static final String INSERT_LIKE = "INSERT INTO film_user_like (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_user_like WHERE film_id = ? AND user_id = ?";

    private static final String FIND_MOST_POPULAR = "SELECT f.* FROM films f JOIN (SELECT film_id," +
            " COUNT(user_id) AS like_count FROM film_user_like GROUP BY film_id) AS l " +
            "ON f.id = l.film_id ORDER BY l.like_count DESC LIMIT ?";


    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreDbStorage genreStorage;
    private final RatingDbStorage ratingStorage;

    @Override
    public Collection<FilmDto> findAll() {
        List<Film> films = jdbc.query(FIND_ALL_QUERY, mapper);
        List<FilmDto> dtoList = new ArrayList<>();
        if (films != null) {
            dtoList = films.stream()
                    .map(film -> FilmMapper.convertToDto(film, genreStorage.getGenresByFilmId(film.getId())))
                    .toList();
        }
        return dtoList;
    }

    @Override
    public FilmDto create(NewFilmRequest request) {
        Rating ratingById = ratingStorage.getRatingById(request.getMpa().getId());
        Set<Long> likes = new TreeSet<>();
        validateFilmDateRelease(request.getReleaseDate());
        if (request.hasGenre()) {
            validateGenreId(request.getGenres());
        }
        Film film = FilmMapper.mapToFilm(request, ratingById, likes);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setInt(5, request.getMpa().getId());

            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        Set<Genre> genre = film.getGenre();
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
        TreeSet<Genre> genres = genreStorage.getGenresByFilmId(id);
        FilmDto dto = FilmMapper.convertToDto(film, genres);
        return dto;
    }

    private Set<Long> getLikesById(Long id) {
        String query = "SELECT user_id FROM film_user_like WHERE film_id = ?";
        List<Long> usersLike = jdbc.queryForList(query, Long.class, id);
        return new HashSet<>(usersLike);
    }

    @Override
    public FilmDto update(UpdateFilmRequest newFilm) {
        Film oldFilm = getFilmById(newFilm.getId());
        Rating rating = oldFilm.getRating();
        if (newFilm.hasReleaseDate()) {
            validateFilmDateRelease(newFilm.getReleaseDate());
        }
        if (newFilm.hasGenre()) {
            validateGenreId(newFilm.getGenres());
        }
        if (newFilm.hasRating()) {
            rating = ratingStorage.getRatingById(newFilm.getMpa().getId());
        }
        Film updatedFilm = FilmMapper.updateFilmFields(oldFilm, newFilm, rating);
        int update1 = jdbc.update(UPDATE_QUERY,
                updatedFilm.getName(),
                updatedFilm.getDescription(),
                updatedFilm.getReleaseDate(),
                updatedFilm.getDuration(),
                updatedFilm.getRating().getId(),
                updatedFilm.getId());
        if (update1 == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }

        Set<Genre> genre = updatedFilm.getGenre();
        jdbc.update(DELETE_FILM_GENRES, updatedFilm.getId());
        for (Genre g : genre) {
            int update2 = jdbc.update(INSERT_FILM_GENRE, updatedFilm.getId(), g.getId());
            String message = update2 == 1 ? "Жанры удалось обновить" : "Жанры не удалось обновить";
            log.info(message);
        }
        FilmDto dto = FilmMapper.convertToDto(updatedFilm, updatedFilm.getGenre());

        return dto;
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
    public Collection<FilmDto> getPopularFilms(Integer count) {
        List<Film> films = jdbc.query(FIND_MOST_POPULAR, mapper, count);
        List<FilmDto> dtoList = new ArrayList<>();
        if (films != null) {
            dtoList = films.stream()
                    .map(film -> FilmMapper.convertToDto(film, genreStorage.getGenresByFilmId(film.getId())))
                    .toList();
        }
        return dtoList;
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return film;
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = String.format("Фильм с id %d не найден", id);
            log.warn(errorMessage);
            throw new FilmNotFoundException(errorMessage);
        }
    }

    @Override
    public FilmDto getFilmDtoById(Long id) {
        Film film = getFilmById(id);
        FilmDto dto = FilmMapper.convertToDto(film, film.getGenre());
        return dto;
    }

    private void validateFilmDateRelease(LocalDate releaseDate) {
        if (releaseDate.isBefore(LocalDate.parse("1895-12-28"))) {
            String errorMessage = "Дата релиза — не раньше 28 декабря 1895 года";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    private void validateGenreId(TreeSet<Genre> genres) {
        Integer maxId = jdbc.queryForObject(FIND_MAX_GENRE_ID, Integer.class);
        for (Genre genre : genres) {
            if (genre.getId() > maxId) {
                String errorMessage = String.format("Рейтинг с id %d не найден", genre.getId());
                log.warn(errorMessage);
                throw new GenreNotFoundException(errorMessage);
            }
        }

    }
}
