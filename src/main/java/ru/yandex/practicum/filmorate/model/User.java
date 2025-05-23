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
public class User {
    Long id;
    @Email
    @NotBlank(message = "Email не должен быть null или пустым")
    String email;
    @NotBlank(message = "Login не должен быть null или пустым")
    @Pattern(regexp = "\\S*", message = "Login не должен содержать пробелы")
    String login;
    String name;
    @Past
    @NotNull
    LocalDate birthday;
    Set<Long> friends = new HashSet<>();
}