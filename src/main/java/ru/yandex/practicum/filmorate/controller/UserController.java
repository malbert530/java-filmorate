package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен HTTP-запрос на получение всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Получен HTTP-запрос на получение пользователя по id {}", id);
        User userById = userService.getUserById(id);
        log.info("Успешно обработан HTTP-запрос на получение пользователя по id: {}", userById);
        return userById;
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getUserFriends(@PathVariable Long id) {
        log.info("Получен HTTP-запрос на получение списка друзей пользователя с id {}", id);
        Collection<User> userFriends = userService.getUserFriends(id);
        log.info("Успешно обработан HTTP-запрос на получение списка друзей пользователя с id: {}", id);
        return userFriends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получен HTTP-запрос на получение списка общих друзей пользователя с id {} и id {}", id, otherId);
        Collection<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.info("Успешно обработан HTTP-запрос на получение списка общих друзей пользователя с id {} и id {}", id, otherId);
        return commonFriends;
    }

    @PostMapping
    public User create(@RequestBody @Valid User user) {
        log.info("Получен HTTP-запрос на создание пользователя: {}", user);
        User createdUser = userService.create(user);
        log.info("Успешно обработан HTTP-запрос на создание пользователя: {}", createdUser);
        return createdUser;
    }

    @PutMapping
    public User update(@RequestBody @Valid UpdateUserRequest request) {
        log.info("Получен HTTP-запрос на обновление пользователя: {}", request);
        User updatedUser = userService.update(request);
        log.info("Успешно обработан HTTP-запрос на обновление пользователя: {}", updatedUser);
        return updatedUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен HTTP-запрос на добавление в друзья пользователей с id {} и id {}", id, friendId);
        User user = userService.addFriend(id, friendId);
        return user;
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен HTTP-запрос на удаление из друзей пользователей с id {} и id {}", id, friendId);
        User user = userService.deleteFriend(id, friendId);
        return user;
    }
}