package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

public interface FeedEventStorage {
    List<FeedEvent> getFeed(Long userId);

    FeedEvent addToFeed(FeedEvent feedEvent);

    void insertFilmEntityToFeed(Long eventId, Long filmId);

    void insertUserEntityToFeed(Long eventId, Long userId);

    void insertReviewEntityToFeed(Long eventId, Long reviewId);
}
