package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Director implements Comparable<Director> {
    private Long id;
    @NotBlank
    private String name;

    @Override
    public int compareTo(Director o) {
        return id.compareTo(o.getId());
    }
}
