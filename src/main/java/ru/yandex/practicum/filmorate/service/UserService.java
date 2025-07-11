package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FeedEventDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FeedEventMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.feed.EventTypeDbStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedEventStorage;
import ru.yandex.practicum.filmorate.storage.feed.OperationDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedEventStorage feedStorage;

    private final Map<String, Integer> operations;
    private final Map<String, Integer> eventTypes;

    public UserService(UserStorage userStorage, FilmStorage filmStorage,
                       FeedEventStorage feedStorage, OperationDbStorage operationStorage, EventTypeDbStorage eventTypeStorage) {
        this.userStorage = userStorage;
        this.feedStorage = feedStorage;
        this.filmStorage = filmStorage;
        operations = operationStorage.getMap();
        eventTypes = eventTypeStorage.getMap();
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        if (userStorage.checkEmailExist(user.getEmail())) {
            String errorMessage = String.format("Email %s уже существует", user.getEmail());
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        return userStorage.create(user);
    }

    public User update(UpdateUserRequest newUser) {
        User userToUpdate = userStorage.getUserById(newUser.getId());
        if (newUser.hasEmail() && userStorage.checkEmailExist(newUser.getEmail())) {
            String errorMessage = String.format("Email %s уже существует", newUser.getEmail());
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        User updatedUser = UserMapper.updateUserFields(userToUpdate, newUser);
        return userStorage.update(updatedUser);
    }

    public User deleteById(Long id) {
        User deletedUser = userStorage.getUserById(id);
        userStorage.deleteById(id);
        return deletedUser;
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.addFriend(id, friendId);
        addFriendToFeed(id, friendId, "ADD");
        return user;
    }

    public User deleteFriend(Long id, Long friendId) {
        User deletedFriendUser = userStorage.deleteFriend(id, friendId);
        addFriendToFeed(id, friendId, "REMOVE");
        return deletedFriendUser;
    }

    public Collection<User> getUserFriends(Long id) {
        return userStorage.getUserFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public List<FilmDto> getRecommendations(Long userId) {
        userStorage.getUserById(userId);

        Map<Long, Set<Long>> allLikes = userStorage.getAllLikesFromDb();

        if (!allLikes.containsKey(userId) || allLikes.get(userId).isEmpty()) {
            log.info("У пользователя {} нет лайков", userId);
            return Collections.emptyList();
        }

        Set<Long> similarUsers = findMostSimilarUsers(userId, allLikes);

        if (similarUsers.isEmpty()) {
            log.info("Не найдено похожих пользователей для пользователя {}", userId);
            return Collections.emptyList();
        }

        Set<Long> recommendations = getRecommendationsFromSimilarUsers(userId, similarUsers, allLikes);

        List<Film> films = filmStorage.getFilmsByIds(recommendations);

        return films.stream()
                .map(FilmMapper::convertToDto)
                .collect(Collectors.toList());
    }


    private Set<Long> findMostSimilarUsers(Long userId, Map<Long, Set<Long>> allLikes) {

        Set<Long> userLikes = allLikes.get(userId);

        Set<Long> mostSimilarUsers = new HashSet<>();
        int maxCommonLikes = 0;

        for (Map.Entry<Long, Set<Long>> entry : allLikes.entrySet()) {
            Long currentUserId = entry.getKey();

            if (currentUserId.equals(userId) || entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }

            int commonLikes = countIntersection(userLikes, entry.getValue());

            if (commonLikes > maxCommonLikes) {
                maxCommonLikes = commonLikes;
                mostSimilarUsers.clear();
                mostSimilarUsers.add(currentUserId);
            } else if (commonLikes == maxCommonLikes && maxCommonLikes > 0) {
                mostSimilarUsers.add(currentUserId);
            }
        }

        return mostSimilarUsers;
    }

    public List<FeedEventDto> getFeed(Long userId) {
        userStorage.getUserById(userId);
        return feedStorage.getFeed(userId).stream().map(FeedEventMapper::convertToDto).toList();
    }

    private Set<Long> getRecommendationsFromSimilarUsers(Long userId, Set<Long> similarUsers, Map<Long, Set<Long>> allLikes) {
        Set<Long> userLikes = allLikes.get(userId);
        Set<Long> recommendations = new HashSet<>();

        for (Long similarUserId : similarUsers) {
            Set<Long> similarUserLikes = allLikes.get(similarUserId);
            if (similarUserLikes != null) {
                recommendations.addAll(
                        similarUserLikes.stream()
                                .filter(filmId -> !userLikes.contains(filmId))
                                .collect(Collectors.toSet())
                );
            }
        }

        return recommendations;
    }

    private int countIntersection(Set<Long> user1Likes, Set<Long> user2Likes) {
        if (user1Likes == null || user2Likes == null) {
            return 0;
        }
        Set<Long> intersection = new HashSet<>(user1Likes);
        intersection.retainAll(user2Likes);
        return intersection.size();
    }

    private void addFriendToFeed(Long id, Long friendId, String operation) {
        FeedEvent feedEvent = FeedEvent.builder()
                .timestamp(Timestamp.from(Instant.now()))
                .userId(id)
                .eventType(new EventType(eventTypes.get("FRIEND"), null))
                .operation(new Operation(operations.get(operation), null))
                .entityId(friendId)
                .build();
        feedStorage.addToFeed(feedEvent);
    }
}