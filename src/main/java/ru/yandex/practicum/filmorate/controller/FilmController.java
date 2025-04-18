package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен HTTP-запрос на получение всех фильмов");
        return films.values();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        log.info("Получен HTTP-запрос на создание фильма: {}", film);
        validateFilmDateRelease(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Успешно обработан HTTP-запрос на создание фильма: {}", film);
        return film;
    }

    private static void validateFilmDateRelease(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))) {
            String errorMessage = "Дата релиза — не раньше 28 декабря 1895 года";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film newFilm) {
        if (newFilm.getId() == null) {
            String errorMessage = "Id должен быть указан";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (films.containsKey(newFilm.getId())) {
            validateFilmDateRelease(newFilm);
            Film oldFilm = films.get(newFilm.getId());
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Успешно обработан HTTP-запрос на обновление фильма: {}", newFilm);
            return oldFilm;
        }
        String errorMessage = String.format("Фильм с id %d не найден", newFilm.getId());
        log.warn(errorMessage);
        throw new ValidationException(errorMessage);
    }
}
