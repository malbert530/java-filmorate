package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    void deleteById(Long id);

    User getUserById(Long id);

    User addFriend(Long id, Long friendId);

    User deleteFriend(Long id, Long friendId);

    Collection<User> getUserFriends(Long id);

    Collection<User> getCommonFriends(Long id, Long otherId);

    boolean checkEmailExist(String email);

    Map<Long, Set<Long>> getAllLikesFromDb();
}
