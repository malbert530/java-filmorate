package ru.yandex.practicum.filmorate.storage.film.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final RatingDbStorage ratingStorage;
    private final GenreDbStorage genreStorage;

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        Timestamp releaseDate = resultSet.getTimestamp("release_date");
        film.setReleaseDate(releaseDate.toLocalDateTime().toLocalDate());
        film.setDuration(resultSet.getInt("duration"));

        Integer ratingId = (Integer) resultSet.getObject("rating_id");
        Rating rating = ratingStorage.getRatingById(ratingId);
        film.setRating(rating);

        TreeSet<Genre> genresByFilmId = genreStorage.getGenresByFilmId(film.getId());
        film.setGenre(genresByFilmId);
        return film;
    }
}
