package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Director implements Comparable<Director> {
    private Long id;
    private String name;

    @Override
    public int compareTo(Director o) {
        return id.compareTo(o.getId());
    }
}
