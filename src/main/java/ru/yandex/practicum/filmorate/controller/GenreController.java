package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public Collection<Genre> getAll() {
        log.info("Получен HTTP-запрос на получение всех жанров");
        return genreService.getAll();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable Integer id) {
        log.info("Получен HTTP-запрос на получение жанра по id {}", id);
        Genre genreById = genreService.getGenreById(id);
        log.info("Успешно обработан HTTP-запрос на получение жанра по id: {}", genreById);
        return genreById;
    }

}
