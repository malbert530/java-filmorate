package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.TreeSet;

@Data
@AllArgsConstructor
public class FilmDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;
    @NotBlank(message = "Название не должно быть null или пустым")
    String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    Integer duration;
    @NotNull
    LocalDate releaseDate;
    @Positive(message = "Рейтинг фильма должна быть положительным числом")
    Rating mpa;
    TreeSet<Genre> genres;
    TreeSet<Director> directors;
}
