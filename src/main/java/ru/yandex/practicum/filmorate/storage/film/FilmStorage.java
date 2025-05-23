package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    List<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    void putLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    List<Film> getPopularFilms(Integer count);

    Film getFilmById(Long id);

    Collection<Film> getFilmByDirectorIdSortedByYear(Integer id);

    Collection<Film> getFilmByDirectorIdSortedByLikes(Integer id);
}
