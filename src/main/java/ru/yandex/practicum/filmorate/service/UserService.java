package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

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
        return userStorage.deleteById(id);
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


}
