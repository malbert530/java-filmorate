package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {
    private String description;
    private Integer errorCode;
}
