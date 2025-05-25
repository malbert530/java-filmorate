package ru.yandex.practicum.filmorate.storage.review.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getLong("review_id"));
        r.setContent(rs.getString("content"));
        r.setIsPositive(rs.getBoolean("is_positive"));
        r.setUserId(rs.getLong("user_id"));
        r.setFilmId(rs.getLong("film_id"));
        r.setUseful(rs.getInt("useful"));
        return r;
    }
}