package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public final class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setId(request.getId());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setRating(request.getMpa());
        film.setGenre(request.getGenres());
        film.setLikes(new TreeSet<>());
        return film;
    }

    public static FilmDto convertToDto(Film film) {
        return new FilmDto(film.getId(), film.getName(), film.getDescription(), film.getDuration(),
                film.getReleaseDate(), film.getRating(), film.getGenre());
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
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
            film.setRating(request.getMpa());
        }
        if (request.hasGenre()) {
            film.setGenre(request.getGenres());
        }
        return film;
    }
}
