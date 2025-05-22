package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(Review review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        Review existing = getById(review.getReviewId()); // Проверяем и получаем текущий отзыв
        review.setUseful(existing.getUseful());          // Переносим текущее значение полезности
        return reviewStorage.update(review);             // Обновляем отзыв (без смены useful)
    }

    public void delete(Long id) {
        getById(id); // Проверка отзыва
        reviewStorage.delete(id);
    }

    public Review getById(Long id) {
        return reviewStorage.getById(id);
    }

    public List<Review> getAll(Long filmId, int count) {
        if (filmId != null) {
            filmStorage.getFilmById(filmId); // Проверка фильма
        }
        return reviewStorage.getAll(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);      // Проверка пользователя
        getById(reviewId);                    // Проверка отзыва
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);      // Проверка пользователя
        getById(reviewId);                    // Проверка отзыва
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);      // Проверка пользователя
        getById(reviewId);                    // Проверка отзыва
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);      // Проверка пользователя
        getById(reviewId);                    // Проверка отзыва
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateUserAndFilm(Long userId, Long filmId) {
        userStorage.getUserById(userId);
        filmStorage.getFilmById(filmId);
    }
}