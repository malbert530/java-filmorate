package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен HTTP-запрос на получение всех пользователей");
        return users.values();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PostMapping
    public User create(@RequestBody @Valid User user) {
        log.info("Получен HTTP-запрос на создание пользователя: {}", user);
        checkEmailExist(user);
        checkNameNullOrBlank(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Успешно обработан HTTP-запрос на создание пользователя: {}", user);
        return user;
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


    @PutMapping
    public User update(@RequestBody @Valid User newUser) {
        log.info("Получен HTTP-запрос на обновление пользователя: {}", newUser);
        if (Objects.isNull(newUser.getId())) {
            String errorMessage = "Id должен быть указан";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (!users.containsKey(newUser.getId())) {
            String errorMessage = String.format("Пользователь с id %d не найден", newUser.getId());
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
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
        log.info("Успешно обработан HTTP-запрос на обновление пользователя: {}", newUser);
        return oldUser;
    }
}