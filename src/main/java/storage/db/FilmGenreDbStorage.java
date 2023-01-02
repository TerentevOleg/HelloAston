package storage.db;

import model.Genre;
import storage.FilmGenreStorage;
import storage.SqlConnection;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class FilmGenreDbStorage implements FilmGenreStorage {

    private static Connection connection = SqlConnection.createConnection();

    @Override
    public void addFilmGenres(long filmId, List<Genre> genres) {
        if (Objects.isNull(genres) || genres.size() < 1) {
            return;
        }
        try {
            String sql = "MERGE INTO film_genre (film_id, genre_id) VALUES " +
                    genres.stream()
                            .map(genre -> String.format("(%d, %d)", filmId, genre.getId()))
                            .collect(Collectors.joining(", "));
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("FilmGenreDbStorage: addFilmGenres sql exception.");
        }
    }

    @Override
    public void deleteFilmGenres(long filmId) {
        try {
            String sql = "DELETE FROM film_genre WHERE film_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, filmId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("FilmGenreDbStorage: deleteFilmGenres sql exception.");
        }
    }

    @Override
    public List<Genre> getGenresByFilmId(long filmId) {
        List<Genre> genreFilm = new ArrayList<>();
        try {
            String sql = "SELECT g.id, g.name " +
                    "FROM film_genre AS fg " +
                    "JOIN genre AS g ON g.id=fg.genre_id " +
                    "WHERE fg.film_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, filmId);

            ResultSet resultSet = preparedStatement.executeQuery();
            genreFilm.add(new Genre(resultSet.getInt("id"), resultSet.getString("name")));
        } catch (SQLException e) {
            throw new RuntimeException("FilmGenreDbStorage: getGenresByFilmId sql exception.");
        }
        return genreFilm;
    }

    @Override
    public Map<Long, List<Genre>> getAllFilmGenres() {
        Map<Long, List<Genre>> genresByFilm = new HashMap<>();
        try {
            String sql = "SELECT f.id AS film_id, g.id, g.name " +
                    "FROM film AS f " +
                    "JOIN film_genre AS fg ON fg.film_id=f.id " +
                    "JOIN genre AS g ON g.id=fg.genre_id";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            long filmId = resultSet.getLong("film_id");
            List<Genre> genres = genresByFilm.get(filmId);
            if (Objects.isNull(genres)) {
                genres = new ArrayList<>();
                genresByFilm.put(filmId, genres);
            }
            genres.add(new Genre(resultSet.getInt("id"), resultSet.getString("name")));
            while (resultSet.next()) {
                genresByFilm.put(filmId, genres);
            }
        } catch (SQLException e) {
            throw new RuntimeException("FilmGenreDbStorage: getAllFilmGenres sql exception.");
        }
        return genresByFilm;
    }

    @Override
    public Map<Long, List<Genre>> getGenresByFilmIds(List<Long> filmIds) {
        Map<Long, List<Genre>> genresByFilm = new HashMap<>();
        try {
            String sql = "SELECT f.id AS film_id, g.id, g.name " +
                    "FROM film AS f " +
                    "JOIN film_genre AS fg ON fg.film_id=f.id " +
                    "JOIN genre AS g ON g.id=fg.genre_id " +
                    "WHERE f.id IN (" +
                    filmIds.stream().map(String::valueOf).collect(Collectors.joining(",")) +
                    ")";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            long filmId = resultSet.getLong("film_id");
            List<Genre> genres = genresByFilm.get(filmId);
            if (Objects.isNull(genres)) {
                genres = new ArrayList<>();
                genresByFilm.put(filmId, genres);
            }
            genres.add(new Genre(resultSet.getInt("id"), resultSet.getString("name")));
            while (resultSet.next()) {
                genresByFilm.put(filmId, genres);
            }
        } catch (SQLException e) {
            throw new RuntimeException("FilmGenreDbStorage: getGenresByFilmIds sql exception.");
        }
        return genresByFilm;
    }
}
