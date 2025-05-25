package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.TreeSet;

@Data
public class UpdateFilmRequest {
    @NotNull(message = "id пользователя должен быть указан")
    Long id;
    String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;
    LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    Integer duration;

    Rating mpa;
    TreeSet<Genre> genres;
    TreeSet<Director> directors;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasDuration() {
        return duration != null;
    }

    public boolean hasRating() {
        return mpa != null;
    }

    public boolean hasGenre() {
        return genres != null;
    }

    public boolean hasDirector() {
        return directors != null;
    }
}
