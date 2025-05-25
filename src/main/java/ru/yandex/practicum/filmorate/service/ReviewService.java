package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.EventTypeDbStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedEventStorage;
import ru.yandex.practicum.filmorate.storage.feed.OperationDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    private final FeedEventStorage feedStorage;

    private final Map<String, Integer> operations;
    private final Map<String, Integer> eventTypes;

    public ReviewService(ReviewStorage reviewStorage, UserStorage userStorage, FilmStorage filmStorage, FeedEventStorage feedStorage, OperationDbStorage operationStorage, EventTypeDbStorage eventTypeStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.feedStorage = feedStorage;
        operations = operationStorage.getMap();
        eventTypes = eventTypeStorage.getMap();
    }

    public Review create(Review review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        Review createdReview = reviewStorage.create(review);
        FeedEvent feedEvent = FeedEvent.builder()
                .timestamp(Timestamp.from(Instant.now()))
                .userId(review.getUserId())
                .eventType(new EventType(eventTypes.get("REVIEW"), null))
                .operation(new Operation(operations.get("ADD"), null))
                .entityId(review.getReviewId())
                .build();
        feedStorage.addToFeed(feedEvent);
        return createdReview;
    }

    public Review update(Review review) {
        Review existing = getById(review.getReviewId());
        review.setUserId(existing.getUserId());
        review.setFilmId(existing.getFilmId());
        review.setUseful(existing.getUseful());
        Review updatedReview = reviewStorage.update(review);
        FeedEvent feedEvent = FeedEvent.builder()
                .timestamp(Timestamp.from(Instant.now()))
                .userId(review.getUserId())
                .eventType(new EventType(eventTypes.get("REVIEW"), null))
                .operation(new Operation(operations.get("UPDATE"), null))
                .entityId(review.getReviewId())
                .build();
        feedStorage.addToFeed(feedEvent);
        return updatedReview;
    }

    public void delete(Long id) {
        Review review = getById(id);
        FeedEvent feedEvent = FeedEvent.builder()
                .timestamp(Timestamp.from(Instant.now()))
                .userId(review.getUserId())
                .eventType(new EventType(eventTypes.get("REVIEW"), null))
                .operation(new Operation(operations.get("REMOVE"), null))
                .entityId(id)
                .build();
        feedStorage.addToFeed(feedEvent);
        reviewStorage.delete(id);
    }

    public Review getById(Long id) {
        return reviewStorage.getById(id);
    }

    public List<Review> getAll(Long filmId, int count) {
        if (filmId != null) {
            filmStorage.getFilmById(filmId);
        }
        return reviewStorage.getAll(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);
        Review review = getById(reviewId);
        reviewStorage.addLike(reviewId, userId);
        FeedEvent feedEvent = FeedEvent.builder()
                .timestamp(Timestamp.from(Instant.now()))
                .userId(review.getUserId())
                .eventType(new EventType(eventTypes.get("LIKE"), null))
                .operation(new Operation(operations.get("ADD"), null))
                .entityId(reviewId)
                .build();
        feedStorage.addToFeed(feedEvent);
    }

    public void addDislike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);
        getById(reviewId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);
        Review review = getById(reviewId);
        reviewStorage.removeLike(reviewId, userId);
        FeedEvent feedEvent = FeedEvent.builder()
                .timestamp(Timestamp.from(Instant.now()))
                .userId(review.getUserId())
                .eventType(new EventType(eventTypes.get("LIKE"), null))
                .operation(new Operation(operations.get("REMOVE"), null))
                .entityId(reviewId)
                .build();
        feedStorage.addToFeed(feedEvent);
    }

    public void removeDislike(Long reviewId, Long userId) {
        userStorage.getUserById(userId);
        getById(reviewId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateUserAndFilm(Long userId, Long filmId) {
        userStorage.getUserById(userId);
        filmStorage.getFilmById(filmId);
    }
}