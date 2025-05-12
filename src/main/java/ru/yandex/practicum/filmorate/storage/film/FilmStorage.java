package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<FilmDto> findAll();

    FilmDto create(NewFilmRequest film);

    FilmDto update(UpdateFilmRequest newFilm);

    void putLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    Collection<FilmDto> getPopularFilms(Integer count);

    Film getFilmById(Long id);

    FilmDto getFilmDtoById(Long id);
}
