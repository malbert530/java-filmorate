package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.mapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_USER_FRIENDS_ID = "SELECT friend_id FROM friends WHERE user_id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT COUNT(*) FROM users WHERE email = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friends(user_id, friend_id) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE id = ?";
    private static final String FIND_USER_FRIENDS = "SELECT * FROM users " +
            "WHERE id IN(SELECT friend_id FROM friends WHERE user_id = ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String GET_LIKES = "SELECT user_id, film_id FROM film_user_like";

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    @Override
    public Collection<User> findAll() {
        List<User> users = jdbc.query(FIND_ALL_QUERY, mapper);
        for (User user : users) {
            List<Long> friends = jdbc.queryForList(FIND_USER_FRIENDS_ID, Long.class, user.getId());
            user.setFriends(new HashSet<>(friends));
        }
        return users;
    }

    @Override
    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            user.setId(id);
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }

        return user;
    }

    @Override
    public User update(User newUser) {
        int rowsUpdated = jdbc.update(UPDATE_QUERY, newUser.getEmail(), newUser.getLogin(), newUser.getName(),
                newUser.getBirthday(), newUser.getId());
        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }
        return newUser;
    }

    @Override
    public User getUserById(Long id) {
        try {
            User user = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return user;
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = String.format("Пользователь с id %d не найден", id);
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }
    }

    @Override
    public boolean checkEmailExist(String email) {
        try {
            Integer count = jdbc.queryForObject(FIND_BY_EMAIL_QUERY, Integer.class, email);
            return count == 1;
        } catch (DataAccessException e) {
            throw new RuntimeException("Ошибка получения данных из таблицы");
        }
    }

    @Override
    public User addFriend(Long id, Long friendId) {
        User user = getUserById(id);
        getUserById(friendId);
        int update = jdbc.update(INSERT_FRIEND_QUERY, id, friendId);
        user.getFriends().add(friendId);
        return user;
    }

    @Override
    public User deleteFriend(Long id, Long friendId) {
        User user = getUserById(id);
        getUserById(friendId);
        int delete = jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
        user.getFriends().remove(friendId);
        return user;
    }

    @Override
    public Collection<User> getUserFriends(Long id) {
        getUserById(id);
        List<User> users = jdbc.query(FIND_USER_FRIENDS, mapper, id);
        if (!users.isEmpty()) {
            for (User user : users) {
                List<Long> friends = jdbc.queryForList(FIND_USER_FRIENDS_ID, Long.class, user.getId());
                user.setFriends(new HashSet<>(friends));
            }
        }
        return users;
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        getUserById(id);
        getUserById(otherId);

        List<User> firstUserFriends = jdbc.query(FIND_USER_FRIENDS, mapper, id);
        List<User> secondUserFriends = jdbc.query(FIND_USER_FRIENDS, mapper, otherId);
        firstUserFriends.retainAll(secondUserFriends);
        List<User> commonFriends = new ArrayList<>(firstUserFriends);
        if (!commonFriends.isEmpty()) {
            for (User user : commonFriends) {
                List<Long> friends = jdbc.queryForList(FIND_USER_FRIENDS_ID, Long.class, user.getId());
                user.setFriends(new HashSet<>(friends));
            }
        }
        return commonFriends;
    }

    @Override
    public Map<Long, Set<Long>> getAllLikesFromDb() {
        return jdbc.query(GET_LIKES, (ResultSetExtractor<Map<Long, Set<Long>>>) rs -> {
            Map<Long, Set<Long>> userLikes = new HashMap<>();
            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                Long filmId = rs.getLong("film_id");
                userLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
            }
            return userLikes;
        });
    }
}
