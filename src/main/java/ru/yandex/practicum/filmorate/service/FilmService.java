package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.feed.EventTypeDbStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedEventStorage;
import ru.yandex.practicum.filmorate.storage.feed.OperationDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Qualifier("userDbStorage")
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final RatingDbStorage ratingStorage;
    private final DirectorDbStorage directorStorage;
    private final FeedEventStorage feedStorage;

    private final Map<String, Integer> operations;
    private final Map<String, Integer> eventTypes;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       GenreDbStorage genreStorage, RatingDbStorage ratingStorage, FeedEventStorage feedStorage,
                       OperationDbStorage operationStorage, EventTypeDbStorage eventTypeStorage, DirectorDbStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.ratingStorage = ratingStorage;
        this.feedStorage = feedStorage;
        this.directorStorage = directorStorage;
        operations = operationStorage.getMap();
        eventTypes = eventTypeStorage.getMap();
    }

    public Collection<FilmDto> findAll() {
        List<FilmDto> dtoList = new ArrayList<>();
        List<Film> films = filmStorage.findAll();
        if (films != null) {
            dtoList = films.stream()
                    .map(FilmMapper::convertToDto)
                    .toList();
        }
        return dtoList;
    }

    public FilmDto create(NewFilmRequest request) {
        validateRatingIdAndUpdateName(request.getMpa());
        validateFilmDateRelease(request.getReleaseDate());
        if (request.hasGenre()) {
            validateGenreIdAndUpdateName(request.getGenres());
        }
        if (request.hasDirector()) {
            validateDirectorIdAndUpdateName(request.getDirectors());
        }
        Film film = FilmMapper.mapToFilm(request);

        Film createdFilm = filmStorage.create(film);
        return FilmMapper.convertToDto(createdFilm);
    }

    public FilmDto update(UpdateFilmRequest newFilm) {

        Film oldFilm = filmStorage.getFilmById(newFilm.getId());
        if (newFilm.hasReleaseDate()) {
            validateFilmDateRelease(newFilm.getReleaseDate());
        }
        if (newFilm.hasGenre()) {
            validateGenreIdAndUpdateName(newFilm.getGenres());
        }
        if (newFilm.hasDirector()) {
            validateDirectorIdAndUpdateName(newFilm.getDirectors());
        }
        if (newFilm.hasRating()) {
            validateRatingIdAndUpdateName(newFilm.getMpa());
        }
        Film film = FilmMapper.updateFilmFields(oldFilm, newFilm);

        Film updated = filmStorage.update(film);
        return FilmMapper.convertToDto(updated);
    }

    public FilmDto deleteById(Long id) {
        Film deletedFilm = filmStorage.getFilmById(id);
        filmStorage.deleteById(id);
        return FilmMapper.convertToDto(deletedFilm);
    }

    public void putLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.putLike(id, userId);
        addFilmLikeToFeed(userId, id, "ADD");
    }

    public void deleteLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.deleteLike(id, userId);
        addFilmLikeToFeed(userId, id, "REMOVE");
    }

    public List<FilmDto> getPopularFilms(Integer count, Integer genreId, Integer year) {
        if (genreId != null) {
            genreStorage.getGenreById(genreId);
        }

        List<Film> films = filmStorage.getPopularFilms(count, genreId, year);
        return films.stream()
                .map(FilmMapper::convertToDto)
                .toList();
    }

    public FilmDto getFilmById(Long id) {
        Film film = filmStorage.getFilmById(id);
        return FilmMapper.convertToDto(film);
    }

    private void validateFilmDateRelease(LocalDate releaseDate) {
        if (releaseDate.isBefore(LocalDate.parse("1895-12-28"))) {
            String errorMessage = "Дата релиза — не раньше 28 декабря 1895 года";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    private void validateGenreIdAndUpdateName(TreeSet<Genre> genres) {
        for (Genre genre : genres) {
            Genre genreWithName = genreStorage.getGenreById(genre.getId());
            genre.setName(genreWithName.getName());
        }
    }

    private void validateDirectorIdAndUpdateName(TreeSet<Director> directors) {
        for (Director director : directors) {
            Director directorWithName = directorStorage.getDirectorById(director.getId());
            director.setName(directorWithName.getName());
        }
    }

    private void validateRatingIdAndUpdateName(Rating mpa) {
        Rating ratingWithName = ratingStorage.getRatingById(mpa.getId());
        mpa.setName(ratingWithName.getName());
    }

    public List<FilmDto> getCommonFilms(Long userId, Long friendId) {

        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);
        return commonFilms.stream()
                .map(FilmMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByDirectorId(Long id, String sortBy) {
        directorStorage.getDirectorById(id);
        List<FilmDto> dtoList = new ArrayList<>();
        Collection<Film> films;
        if (sortBy.equalsIgnoreCase("year")) {
            films = filmStorage.getFilmByDirectorIdSortedByYear(id);
        } else if (sortBy.equalsIgnoreCase("likes")) {
            films = filmStorage.getFilmByDirectorIdSortedByLikes(id);
        } else {
            String errorMessage = String.format("Неизвестный параметр запроса - %s", sortBy);
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (films != null) {
            dtoList = films.stream()
                    .map(FilmMapper::convertToDto)
                    .toList();
        }
        return dtoList;
    }

    public List<FilmDto> getFilmsBySearch(String query, List<String> by) {
        List<FilmDto> dtoList = new ArrayList<>();
        List<Film> films = new ArrayList<>();
        if (query.isBlank()) {
            String errorMessage = "Пустой параметр запроса query";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (by.isEmpty()) {
            String errorMessage = "Пустой параметр запроса by";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (by.size() > 2) {
            String errorMessage = String.format("Слишком много параметров в 'by' - %s", by);
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (by.size() == 2) {
            if (by.contains("title") && by.contains("director")) {
                films = filmStorage.getFilmsByDirectorAndTitle(query);
            }
        } else {
            if (by.contains("director")) {
                films = filmStorage.getFilmsByDirector(query);
            } else if (by.contains("title")) {
                films = filmStorage.getFilmsByTitle(query);
            } else {
                String errorMessage = String.format("Неизвестный параметр запроса - %s", by.getFirst());
                log.warn(errorMessage);
                throw new ValidationException(errorMessage);
            }
        }
        if (films != null) {
            dtoList = films.stream()
                    .map(FilmMapper::convertToDto)
                    .toList();
        }
        return dtoList;
    }

    private void addFilmLikeToFeed(Long userId, Long filmId, String operation) {
        FeedEvent feedEvent = FeedEvent.builder()
                .timestamp(Timestamp.from(Instant.now()))
                .userId(userId)
                .eventType(new EventType(eventTypes.get("LIKE"), null))
                .operation(new Operation(operations.get(operation), null))
                .entityId(filmId)
                .build();
        feedStorage.addToFeed(feedEvent);
    }
}
