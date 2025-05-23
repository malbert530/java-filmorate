package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorStorage;

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director getDirectorById(Long id) {
        return directorStorage.getDirectorById(id);
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        Director directorToUpdate = directorStorage.getDirectorById(director.getId());
        if (director.getName() != null && !director.getName().isBlank()) {
            directorToUpdate.setName(director.getName());
        }
        return directorStorage.update(directorToUpdate);
    }

    public Director deleteDirectorById(Long id) {
        Director directorToDelete = directorStorage.getDirectorById(id);
        directorStorage.deleteDirectorById(id);
        return directorToDelete;
    }
}
