package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Film {
    Long id;
    @NotBlank(message = "Название не должно быть null или пустым")
    String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;
    @PastOrPresent
    @NotNull
    LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    int duration;
    Set<Long> likes = new HashSet<>();
    Rate rate;
    Set<Genre> genre = new HashSet<>();
}