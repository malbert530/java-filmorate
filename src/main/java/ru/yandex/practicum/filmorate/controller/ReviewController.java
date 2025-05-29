package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review create(@RequestBody @Valid Review review) {
        log.info("POST /reviews - создание отзыва: {}", review);
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody @Valid Review review) {
        log.info("PUT /reviews - обновление отзыва: {}", review);
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("DELETE /reviews/{} - удаление отзыва", id);
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable Long id) {
        log.info("GET /reviews/{} - получение отзыва по id", id);
        return reviewService.getById(id);
    }

    @GetMapping
    public List<Review> getAll(@RequestParam(required = false) Long filmId,
                               @RequestParam(defaultValue = "10") @Min(1) int count) {
        log.info("GET /reviews?filmId={}&count={} - получение списка отзывов", filmId, count);
        return reviewService.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("PUT /reviews/{}/like/{} - лайк отзыву", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("PUT /reviews/{}/dislike/{} - дизлайк отзыву", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("DELETE /reviews/{}/like/{} - удаление лайка", id, userId);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("DELETE /reviews/{}/dislike/{} - удаление дизлайка", id, userId);
        reviewService.removeDislike(id, userId);
    }
}