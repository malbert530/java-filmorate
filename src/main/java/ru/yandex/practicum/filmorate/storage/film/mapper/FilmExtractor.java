package ru.yandex.practicum.filmorate.storage.film.mapper;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Component
public class FilmExtractor implements ResultSetExtractor<List<Film>> {
    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Film> filmMap = new LinkedHashMap<>();
        while (rs.next()) {
            Long currentFilmId = rs.getLong("id");
            if (!filmMap.containsKey(currentFilmId)) { //если фильм встречается впервые, то считываем строку и добавляем фильм в мапу
                Film film = new Film();
                film.setId(currentFilmId);
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));

                Timestamp releaseDate = rs.getTimestamp("release_date");
                film.setReleaseDate(releaseDate.toLocalDateTime().toLocalDate());
                film.setDuration(rs.getInt("duration"));

                Integer ratingId = (Integer) rs.getObject("rating_id");
                String ratingName = rs.getString("rating_name");
                Rating rating = new Rating(ratingId, ratingName);
                film.setRating(rating);

                Integer genreId = (Integer) rs.getObject("genre_id");
                String genreName = rs.getString("genre_name");
                TreeSet<Genre> genres = new TreeSet<>();
                if (genreId != null) {
                    Genre genre = new Genre(genreId, genreName);
                    genres.add(genre);
                }
                film.setGenre(genres);
                filmMap.put(currentFilmId, film);
            } else {
                TreeSet<Genre> filmGenres = filmMap.get(currentFilmId).getGenre();
                Integer genreId = (Integer) rs.getObject("genre_id");
                String genreName = rs.getString("genre_name");
                Genre genre = new Genre(genreId, genreName);
                filmGenres.add(genre);
            }
        }
        return new ArrayList<>(filmMap.values());
    }
}
