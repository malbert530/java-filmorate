package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    List<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    void deleteById(Long id);

    void putLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    List<Film> getPopularFilms(Integer count, Integer genreId, Integer year);

    Film getFilmById(Long id);

    List<Film> getFilmByDirectorIdSortedByYear(Long id);

    List<Film> getFilmByDirectorIdSortedByLikes(Long id);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getFilmsByIds(Collection<Long> ids);

    List<Film> getFilmsByDirectorAndTitle(String query);

    List<Film> getFilmsByDirector(String query);

    List<Film> getFilmsByTitle(String query);
}
