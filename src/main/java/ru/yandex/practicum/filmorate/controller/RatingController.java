package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @GetMapping
    public Collection<Rating> getAll() {
        log.info("Получен HTTP-запрос на получение всех рейтингов");
        return ratingService.getAll();
    }

    @GetMapping("/{id}")
    public Rating getGenreById(@PathVariable Integer id) {
        log.info("Получен HTTP-запрос на получение рейтинга по id {}", id);
        Rating ratingById = ratingService.getRatingById(id);
        log.info("Успешно обработан HTTP-запрос на получение рейтинга по id: {}", ratingById);
        return ratingById;
    }
}
