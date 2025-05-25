package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Film {
    Long id;
    @NotBlank(message = "Название не должно быть null или пустым")
    String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;
    @NotNull
    LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    Integer duration;
    Set<Long> likes;
    Rating rating;
    TreeSet<Genre> genre;
    TreeSet<Director> directors;
}