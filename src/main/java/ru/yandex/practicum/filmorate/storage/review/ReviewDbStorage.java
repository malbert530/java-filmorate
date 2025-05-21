package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.mapper.ReviewRowMapper;

import java.sql.Statement;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbc;
    private final ReviewRowMapper mapper;

    private static final String INSERT_REVIEW_SQL = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";

    private static final String UPDATE_REVIEW_SQL = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";

    private static final String DELETE_REVIEW_SQL = "DELETE FROM reviews WHERE review_id = ?";

    private static final String SELECT_ALL = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";

    private static final String SELECT_ALL_BY_FILM = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";

    private static final String INSERT_REVIEW_LIKE = "INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, ?)";

    private static final String DELETE_REVIEW_LIKE = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";

    private static final String UPDATE_USEFUL_INCREMENT = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";

    private static final String CHECK_REVIEW_LIKE_EXISTS = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";

    @Override
    public Review create(Review review) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            var ps = connection.prepareStatement(
                    INSERT_REVIEW_SQL,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        return getById(id);
    }

    @Override
    public Review update(Review review) {
        int updated = jdbc.update(UPDATE_REVIEW_SQL,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        if (updated == 0) {
            throw new ReviewNotFoundException("Не удалось обновить отзыв с id = " + review.getReviewId());
        }
        return getById(review.getReviewId());
    }

    @Override
    public void delete(Long id) {
        jdbc.update(DELETE_REVIEW_SQL, id);
    }

    @Override
    public Review getById(Long id) {
        try {
            return jdbc.queryForObject(SELECT_REVIEW_BY_ID, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReviewNotFoundException("Отзыв с id=" + id + " не найден.");
        }
    }

    @Override
    public List<Review> getAll(Long filmId, int count) {
        if (filmId == null) {
            return jdbc.query(SELECT_ALL, mapper, count);
        }
        return jdbc.query(SELECT_ALL_BY_FILM, mapper, filmId, count);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        if (likeExists(reviewId, userId)) {
            throw new ValidationException("Пользователь уже поставил лайк или дизлайк этому отзыву.");
        }
        jdbc.update(INSERT_REVIEW_LIKE, reviewId, userId, true);
        jdbc.update(UPDATE_USEFUL_INCREMENT, 1, reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        if (likeExists(reviewId, userId)) {
            throw new ValidationException("Пользователь уже поставил лайк или дизлайк этому отзыву.");
        }
        jdbc.update(INSERT_REVIEW_LIKE, reviewId, userId, false);
        jdbc.update(UPDATE_USEFUL_INCREMENT, -1, reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        jdbc.update(DELETE_REVIEW_LIKE, reviewId, userId);
        jdbc.update(UPDATE_USEFUL_INCREMENT, -1, reviewId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        jdbc.update(DELETE_REVIEW_LIKE, reviewId, userId);
        jdbc.update(UPDATE_USEFUL_INCREMENT, 1, reviewId);
    }

    private boolean likeExists(Long reviewId, Long userId) {
        Integer count = jdbc.queryForObject(CHECK_REVIEW_LIKE_EXISTS, Integer.class, reviewId, userId);
        return count != null && count > 0;
    }
}