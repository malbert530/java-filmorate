package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен HTTP-запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam Integer count) {
        if (Objects.isNull(count)) {
            count = 10;
        }
        return filmService.getPopularFilms(count);
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        log.info("Получен HTTP-запрос на создание фильма: {}", film);
        Film createdFilm = filmService.create(film);
        log.info("Успешно обработан HTTP-запрос на создание фильма: {}", createdFilm);
        return createdFilm;
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film newFilm) {
        log.info("Получен HTTP-запрос на обновление фильма: {}", newFilm);
        Film updatedFilm = filmService.update(newFilm);
        log.info("Успешно обработан HTTP-запрос на обновление фильма: {}", updatedFilm);
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public Film putLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен HTTP-запрос на добавление лайка для фильма с id {} от пользователя с id {}", id, userId);
        return filmService.putLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен HTTP-запрос на удаление лайка для фильма с id {} от пользователя с id {}", id, userId);
        return filmService.deleteLike(id, userId);
    }
}
