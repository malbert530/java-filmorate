package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
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
    public Collection<FilmDto> findAll() {
        log.info("Получен HTTP-запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable Long id) {
        log.info("Получен HTTP-запрос на получение фильма по id {}", id);
        FilmDto film = filmService.getFilmById(id);
        log.info("Успешно обработан HTTP-запрос на получение фильма по id {}", id);
        return film;
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopularFilms(@RequestParam Integer count) {
        log.info("Получен HTTP-запрос на получение {} самых популярных фильмов", count);
        if (Objects.isNull(count)) {
            count = 10;
        }
        Collection<FilmDto> popularFilms = filmService.getPopularFilms(count);
        log.info("Успешно обработан HTTP-запрос на получение {} самых популярных фильмов", count);
        return popularFilms;
    }

    @PostMapping
    public FilmDto create(@RequestBody @Valid NewFilmRequest request) {
        log.info("Получен HTTP-запрос на создание фильма: {}", request);
        FilmDto createdFilm = filmService.create(request);
        log.info("Успешно обработан HTTP-запрос на создание фильма: {}", createdFilm);
        return createdFilm;
    }

    @PutMapping
    public FilmDto update(@RequestBody @Valid UpdateFilmRequest newFilm) {
        log.info("Получен HTTP-запрос на обновление фильма: {}", newFilm);
        FilmDto updatedFilm = filmService.update(newFilm);
        log.info("Успешно обработан HTTP-запрос на обновление фильма: {}", updatedFilm);
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен HTTP-запрос на добавление лайка для фильма с id {} от пользователя с id {}", id, userId);
        filmService.putLike(id, userId);
        log.info("Успешно обработан HTTP-запрос на добавление лайка для фильма с id {} от пользователя с id {}", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен HTTP-запрос на удаление лайка для фильма с id {} от пользователя с id {}", id, userId);
        filmService.deleteLike(id, userId);
        log.info("Успешно обработан HTTP-запрос на удаление лайка для фильма с id {} от пользователя с id {}", id, userId);
    }
}
