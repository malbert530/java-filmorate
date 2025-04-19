package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    User getUserById(Long id);

    User addFriend(Long id, Long friendId);

    User deleteFriend(Long id, Long friendId);

    Collection<User> getUserFriends(Long id);

    Collection<User> getCommonFriends(Long id, Long otherId);

    User checkUserExistAndGet(Long id);
}
