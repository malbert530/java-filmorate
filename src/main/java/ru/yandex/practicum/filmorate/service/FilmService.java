package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

@Slf4j
@Service
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final RatingDbStorage ratingStorage;

    public Collection<FilmDto> findAll() {
        List<FilmDto> dtoList = new ArrayList<>();
        List<Film> films = filmStorage.findAll();
        if (films != null) {
            dtoList = films.stream()
                    .map(FilmMapper::convertToDto)
                    .toList();
        }
        return dtoList;
    }

    public FilmDto create(NewFilmRequest request) {
        validateRatingIdAndUpdateName(request.getMpa());
        validateFilmDateRelease(request.getReleaseDate());
        if (request.hasGenre()) {
            validateGenreIdAndUpdateName(request.getGenres());
        }
        Film film = FilmMapper.mapToFilm(request);

        Film createdFilm = filmStorage.create(film);
        return FilmMapper.convertToDto(createdFilm);
    }

    public FilmDto update(UpdateFilmRequest newFilm) {

        Film oldFilm = filmStorage.getFilmById(newFilm.getId());
        if (newFilm.hasReleaseDate()) {
            validateFilmDateRelease(newFilm.getReleaseDate());
        }
        if (newFilm.hasGenre()) {
            validateGenreIdAndUpdateName(newFilm.getGenres());
        }
        if (newFilm.hasRating()) {
            validateRatingIdAndUpdateName(newFilm.getMpa());
        }
        Film film = FilmMapper.updateFilmFields(oldFilm, newFilm);

        Film updated = filmStorage.update(film);
        return FilmMapper.convertToDto(updated);
    }

    public void putLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.putLike(id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.deleteLike(id, userId);
    }

    public List<FilmDto> getPopularFilms(Integer count) {
        List<Film> films = filmStorage.getPopularFilms(count);
        List<FilmDto> dtoList = new ArrayList<>();
        if (films != null) {
            dtoList = films.stream()
                    .map(FilmMapper::convertToDto)
                    .toList();
        }
        return dtoList;
    }

    public FilmDto getFilmById(Long id) {
        Film film = filmStorage.getFilmById(id);
        return FilmMapper.convertToDto(film);
    }

    private void validateFilmDateRelease(LocalDate releaseDate) {
        if (releaseDate.isBefore(LocalDate.parse("1895-12-28"))) {
            String errorMessage = "Дата релиза — не раньше 28 декабря 1895 года";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    private void validateGenreIdAndUpdateName(TreeSet<Genre> genres) {
        for (Genre genre : genres) {
            Genre genreWithName = genreStorage.getGenreById(genre.getId());
            genre.setName(genreWithName.getName());
        }

    }

    private void validateRatingIdAndUpdateName(Rating mpa) {
        Rating ratingWithName = ratingStorage.getRatingById(mpa.getId());
        mpa.setName(ratingWithName.getName());
    }
}
