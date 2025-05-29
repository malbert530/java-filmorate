package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

public interface FeedEventStorage {
    List<FeedEvent> getFeed(Long userId);

    void addToFeed(FeedEvent feedEvent);
}
