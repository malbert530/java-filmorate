package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({RatingDbStorage.class})
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate")
class FilmorateApplicationTests {
    private final RatingDbStorage ratingStorage;

    @Test
    public void testFindUserById() {
        Rating rating = ratingStorage.getRatingById(1);
        assertThat(rating).hasFieldOrPropertyWithValue("id", 1);
    }

}
