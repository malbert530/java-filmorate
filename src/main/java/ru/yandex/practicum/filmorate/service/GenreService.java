package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreStorage;

    public Collection<Genre> getAll() {
        return genreStorage.getAll();
    }

    public Genre getGenreById(Integer id) {
        Genre genreById = genreStorage.getGenreById(id);
        return genreById;
    }
}
