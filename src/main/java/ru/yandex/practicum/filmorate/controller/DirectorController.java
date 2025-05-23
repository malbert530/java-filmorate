package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> findAll() {
        log.info("Получен HTTP-запрос на получение всех режиссеров");
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Long id) {
        log.info("Получен HTTP-запрос на получение режиссера по id {}", id);
        Director directorById = directorService.getDirectorById(id);
        log.info("Успешно обработан HTTP-запрос на получение режиссера по id: {}", directorById);
        return directorById;
    }

    @PostMapping
    public Director create(@RequestBody @Valid Director director) {
        log.info("Получен HTTP-запрос на создание режиссера: {}", director);
        Director createdDirector = directorService.create(director);
        log.info("Успешно обработан HTTP-запрос на создание режиссера: {}", createdDirector);
        return createdDirector;
    }

    @PutMapping
    public Director update(@RequestBody @Valid Director request) {
        log.info("Получен HTTP-запрос на обновление режиссера: {}", request);
        Director updatedDirector = directorService.update(request);
        log.info("Успешно обработан HTTP-запрос на обновление режиссера: {}", updatedDirector);
        return updatedDirector;
    }

    @DeleteMapping("/{id}")
    public Director deleteDirectorById(@PathVariable Long id) {
        log.info("Получен HTTP-запрос на удаление режиссера с id {}", id);
        Director director = directorService.deleteDirectorById(id);
        return director;
    }
}
