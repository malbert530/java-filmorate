package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

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

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.addFriend(id, friendId);
        return user;
    }

    public User deleteFriend(Long id, Long friendId) {
        return userStorage.deleteFriend(id, friendId);
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

        Set<Long> recommendations = getRecommendationsFromSimilarUsers(
                userId,
                similarUsers,
                allLikes
        );

        List<FilmDto> recomendations = new ArrayList<>();
        for (Long filmId : recommendations) {
            try {
                Film film = filmStorage.getFilmById(filmId);
                FilmDto filmDto = FilmMapper.convertToDto(film);
                recomendations.add(filmDto);
            } catch (FilmNotFoundException e) {
                log.warn("Фильм с id {} не найден", filmId);
            }
        }
        return recomendations;
    }


    public Set<Long> findMostSimilarUsers(Long userId, Map<Long, Set<Long>> allLikes) {
        if (userId == null || allLikes == null || allLikes.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Long> userLikes = allLikes.get(userId);
        if (userLikes == null || userLikes.isEmpty()) {
            return Collections.emptySet();
        }

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
}
