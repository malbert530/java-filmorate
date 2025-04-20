package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        checkEmailExist(user);
        checkNameNullOrBlank(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        if (Objects.isNull(newUser.getId())) {
            String errorMessage = "Id должен быть указан";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (!users.containsKey(newUser.getId())) {
            String errorMessage = String.format("Пользователь с id %d не найден", newUser.getId());
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }
        User oldUser = users.get(newUser.getId());
        if (!Objects.equals(newUser.getEmail(), users.get(newUser.getId()).getEmail())) {
            checkEmailExist(newUser);
        }
        checkNameNullOrBlank(newUser);
        oldUser.setName(newUser.getName());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setBirthday(newUser.getBirthday());
        return oldUser;
    }

    @Override
    public User getUserById(Long id) {
        return checkUserExistAndGet(id);
    }

    @Override
    public User addFriend(Long id, Long friendId) {
        User user1 = checkUserExistAndGet(id);
        User user2 = checkUserExistAndGet(friendId);
        user1.getFriends().add(friendId);
        user2.getFriends().add(id);
        return user1;
    }

    @Override
    public User checkUserExistAndGet(Long id) {
        if (!users.containsKey(id)) {
            String errorMessage = String.format("Пользователь с id %d не найден", id);
            throw new UserNotFoundException(errorMessage);
        }
        return users.get(id);
    }

    @Override
    public User deleteFriend(Long id, Long friendId) {
        User user1 = checkUserExistAndGet(id);
        User user2 = checkUserExistAndGet(friendId);
        user1.getFriends().remove(friendId);
        user2.getFriends().remove(id);
        return user1;
    }

    @Override
    public Collection<User> getUserFriends(Long id) {
        User user = checkUserExistAndGet(id);
        return user.getFriends().stream()
                .map(users::get)
                .toList();
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user1 = checkUserExistAndGet(id);
        User user2 = checkUserExistAndGet(otherId);
        Set<Long> user2friends = user2.getFriends();
        return user1.getFriends().stream()
                .filter(o1 -> {
                    return user2friends.stream().anyMatch(o1::equals);
                })
                .map(users::get)
                .toList();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private static void checkNameNullOrBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void checkEmailExist(User user) {
        if (users.values().stream().anyMatch(o -> user.getEmail().equals(o.getEmail()))) {
            String errorMessage = String.format("Этот имейл %s уже используется", user.getEmail());
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}
