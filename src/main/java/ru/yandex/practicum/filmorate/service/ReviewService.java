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
        return reviewStorage.update(review);
    }

    public void delete(Long id) {
        reviewStorage.delete(id);
    }

    public Review getById(Long id) {
        return reviewStorage.getById(id);
    }

    public List<Review> getAll(Long filmId, int count) {
        return reviewStorage.getAll(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateUserAndFilm(Long userId, Long filmId) {
        userStorage.getUserById(userId);
        filmStorage.getFilmById(filmId);
    }
}