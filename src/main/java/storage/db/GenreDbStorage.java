package storage.db;

import model.Genre;
import storage.GenreStorage;
import storage.SqlConnection;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class GenreDbStorage implements GenreStorage {

    private static Connection connection = SqlConnection.createConnection();

    @Override
    public List<Genre> getAll() {
        List<Genre> genres = new ArrayList<>();
        try {
            String sql = "SELECT id, name FROM genre";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                genres.add(makeGenre(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("GenreDbStorage: getAll sql exception.");
        }
        return genres;
    }

    @Override
    public Optional<Genre> getById(long id) {
        Genre genre = null;
        try {
            String sql = "SELECT id, name FROM genre WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            genre = makeGenre(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException("GenreDbStorage: getById sql exception.");
        }
        return Optional.ofNullable(genre);
    }

    @Override
    public Map<Integer, Genre> getByIds(List<Integer> genreIds) {
        Map<Integer, Genre> genres = new HashMap<>();
        try {
            String sql = "SELECT id, name FROM genre WHERE id IN (" +
                    genreIds.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ")";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                genres.put(id, new Genre(id, name));
            }
        } catch (SQLException e) {
            throw new RuntimeException("GenreDbStorage: getByIds sql exception.");
        }
        return genres;
    }

    private static Genre makeGenre(ResultSet resultSet) throws SQLException {
        return new Genre(resultSet.getInt("id"), resultSet.getString("name"));
    }
}
