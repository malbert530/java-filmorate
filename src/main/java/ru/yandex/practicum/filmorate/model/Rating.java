package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rating implements Comparable<Rating> {
    private int id;
    private String name;

    @Override
    public int compareTo(Rating o) {
        return id - o.id;
    }
}
