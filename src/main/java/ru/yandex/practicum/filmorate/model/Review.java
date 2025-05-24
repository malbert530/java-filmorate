package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    private Long reviewId;

    @NotBlank(message = "Текст отзыва не должен быть пустым")
    private String content;

    @NotNull(message = "Поле isPositive обязательно")
    private Boolean isPositive;

    @NotNull(message = "Не указан пользователь")
    private Long userId;

    @NotNull(message = "Не указан фильм")
    private Long filmId;

    private Integer useful;
}