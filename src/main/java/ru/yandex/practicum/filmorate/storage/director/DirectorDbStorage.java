package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.mapper.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectorDbStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE id = ?";


    private final JdbcTemplate jdbc;
    private final DirectorRowMapper mapper;

    public Collection<Director> findAll() {
        List<Director> directors = jdbc.query(FIND_ALL_QUERY, mapper);
        return new TreeSet<>(directors);
    }

    public Director getDirectorById(Integer id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            String errorMessage = String.format("Режиссер с id %d не найден", id);
            log.warn(errorMessage);
            throw new DirectorNotFoundException(errorMessage);
        }
    }

    public Director create(Director director) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);

        if (id != null) {
            director.setId(id);
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }

        return director;
    }

    public Director update(Director directorToUpdate) {
        int rowsUpdated = jdbc.update(UPDATE_QUERY, directorToUpdate.getName(), directorToUpdate.getId());
        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }
        return directorToUpdate;
    }

    public void deleteDirectorById(Integer id) {
        int rowsUpdated = jdbc.update(DELETE_QUERY, id);
        if (rowsUpdated == 0) {
            throw new DirectorNotFoundException("Не удалось обновить данные");
        }
        log.info("Успешно удален режиссер с id {}", id);
    }
}
