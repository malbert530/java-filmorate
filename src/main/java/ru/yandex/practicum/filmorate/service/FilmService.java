package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<FilmDto> findAll() {
        return filmStorage.findAll();
    }

    public FilmDto create(NewFilmRequest request) {
        return filmStorage.create(request);
    }

    public FilmDto update(UpdateFilmRequest newFilm) {
        return filmStorage.update(newFilm);
    }

    public void putLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.putLike(id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.deleteLike(id, userId);
    }

    public Collection<FilmDto> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }

    public FilmDto getFilmById(Long id) {
        return filmStorage.getFilmDtoById(id);
    }
}
