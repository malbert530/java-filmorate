package ru.yandex.practicum.filmorate.storage.feed.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OperationMapExtractor implements ResultSetExtractor<Map<String, Integer>> {
    @Override
    public Map<String, Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
        HashMap<String, Integer> operations = new HashMap<>();
        while (rs.next()) {
            operations.put(rs.getString("name"), rs.getInt("id"));
        }
        return operations;
    }
}
