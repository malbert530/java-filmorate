package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

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
    LocalDate releaseDate;
    @PositiveOrZero(message = "Продолжительность фильма должна быть положительным числом")
    int duration;
}
