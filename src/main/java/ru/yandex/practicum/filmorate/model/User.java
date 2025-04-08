package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class User {
    Long id;
    @Email
    @NotBlank(message = "Email не должен быть null или пустым")
    String email;
    @NotNull(message = "Login не должен быть null")
    @NotBlank(message = "Login не должен быть пустым")
    @Pattern(regexp = "\\S*", message = "Login не должен содержать пробелы")
    String login;
    String name;
    @Past
    LocalDate birthday;
}
