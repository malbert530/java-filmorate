package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);

    Film putLike(Long id, Long userId);

    Film deleteLike(Long id, Long userId);

    Collection<Film> getPopularFilms(Integer count);
}
