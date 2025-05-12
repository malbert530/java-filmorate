package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Set;
import java.util.TreeSet;

public final class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request, Rating rating, Set<Long> likes) {
        Film film = new Film();
        film.setId(request.getId());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setRating(rating);
        film.setGenre(request.getGenres());
        film.setLikes(likes);
        return film;
    }

    public static FilmDto convertToDto(Film film, TreeSet<Genre> genres) {
        return new FilmDto(film.getId(), film.getName(), film.getDescription(), film.getDuration(),
                film.getReleaseDate(), film.getRating(), genres);
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request, Rating rating) {
        if (request.hasName()) {
            film.setName(request.getName());
        }
        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        if (request.hasDescription()) {
            film.setDescription(request.getDescription());
        }
        if (request.hasRating()) {
            film.setRating(rating);
        }
        if (request.hasGenre()) {
            film.setGenre(request.getGenres());
        }
        return film;
    }
}
