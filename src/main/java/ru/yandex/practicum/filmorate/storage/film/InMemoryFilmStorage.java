package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final UserStorage userStorage;

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        validateFilmDateRelease(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            String errorMessage = "Id должен быть указан";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        checkFilmExist(newFilm.getId());
        validateFilmDateRelease(newFilm);
        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());
        return oldFilm;
    }

    private void checkFilmExist(Long id) {
        if (!films.containsKey(id)) {
            String errorMessage = String.format("Фильм с id %d не найден", id);
            log.warn(errorMessage);
            throw new FilmNotFoundException(errorMessage);
        }
    }

    @Override
    public Film putLike(Long id, Long userId) {
        checkFilmExist(id);
        userStorage.checkUserExist(userId);
        films.get(id).getLikes().add(userId);
        return films.get(id);
    }

    @Override
    public Film deleteLike(Long id, Long userId) {
        checkFilmExist(id);
        userStorage.checkUserExist(userId);
        films.get(id).getLikes().remove(userId);
        return films.get(id);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        return films.values().stream().sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size()).limit(count).toList();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private static void validateFilmDateRelease(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))) {
            String errorMessage = "Дата релиза — не раньше 28 декабря 1895 года";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}
