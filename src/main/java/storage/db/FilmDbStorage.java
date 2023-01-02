package storage.db;

import model.Film;
import model.Mpa;
import storage.FilmStorage;
import storage.SqlConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FilmDbStorage implements FilmStorage {

    private static Connection connection = SqlConnection.createConnection();

    @Override
    public List<Film> getAll() {
        List<Film> films = new ArrayList<>();
        try {
            String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                    "m.id AS mpa_id, m.name AS mpa_name, " +
                    "COUNT(DISTINCT l.user_id) AS rate " +
                    "FROM film AS f " +
                    "LEFT JOIN mpa AS m ON m.id=f.mpa_id " +
                    "LEFT JOIN likes AS l ON l.film_id=f.id " +
                    "GROUP BY f.id";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                films.add(makeFilm(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FilmDbStorage: getAll sql exception.");
        }
        return films;
    }

    @Override
    public List<Film> getFilmsLikedByUser(long userId) {
        List<Film> films = new ArrayList<>();
        //Фильмы, которым пользователь поставил лайк
        try {
            String sql = "WITH rates AS\n" +
                    "    (SELECT f.id AS film_id, COUNT(DISTINCT l.user_id) AS rate\n" +
                    "     FROM film AS f\n" +
                    "     LEFT JOIN likes AS l ON l.film_id = f.id\n" +
                    "     GROUP BY f.id)\n" +
                    "SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration,\n" +
                    "       m.id AS mpa_id, m.name AS mpa_name, r.rate\n" +
                    "FROM film AS f\n" +
                    "    LEFT JOIN rates AS r ON r.film_id = f.id\n" +
                    "    LEFT JOIN mpa AS m ON m.id = f.mpa_id\n" +
                    "    LEFT JOIN likes AS l ON l.film_id = f.id\n" +
                    "WHERE l.user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                films.add(makeFilm(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FilmDbStorage: getFilmsLikedByUser sql exception.");
        }
        return films;
    }

    @Override
    public Optional<Film> getById(long id) {
        Film film = null;
        try {
            String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                    "m.id AS mpa_id, m.name AS mpa_name, " +
                    "COUNT(DISTINCT l.user_id) AS rate " +
                    "FROM film AS f " +
                    "LEFT JOIN mpa AS m ON m.id=f.mpa_id " +
                    "LEFT JOIN likes AS l ON l.film_id=f.id " +
                    "WHERE f.id=? " +
                    "GROUP BY f.id";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            film = makeFilm(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException("FilmDbStorage: getById sql exception.");
        }
        return Optional.ofNullable(film);
    }

    @Override
    public long add(Film film) {
        try {
            String sql = "INSERT INTO film(name, description, release_date, duration, mpa_id) VALUES(?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(sql);

            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            if (Objects.nonNull(film.getMpa())) {
                preparedStatement.setInt(5, film.getMpa().getId());
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throw new RuntimeException("FilmDbStorage: add sql exception.");
        }
    }

    @Override
    public boolean update(Film film) {
        try {
            String sql = "UPDATE film " +
                    "SET name=?, description=?, release_date=?, duration=?, mpa_id=? " +
                    "WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            if (Objects.nonNull(film.getMpa())) {
                preparedStatement.setInt(5, film.getMpa().getId());
            }
            preparedStatement.setLong(6, film.getId());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("FilmDbStorage: update sql exception.");
        }
    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        List<Film> commonFilms = new ArrayList<>();
        try {
            String sql =
                    "SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration," +
                            " f.mpa_id, m.name AS mpa_name, f.rate FROM " +
                            "(SELECT film_id " +
                            "FROM likes " +
                            "WHERE user_id = ? " +
                            "INTERSECT SELECT DISTINCT film_id " +
                            "FROM likes " +
                            "WHERE user_id = ?) AS l " +
                            "LEFT JOIN " +
                            "(SELECT film_id, COUNT(user_id) AS rate " +
                            "FROM likes " +
                            "GROUP BY film_id) f ON (f.film_id = l.film_id) " +
                            "JOIN film AS f ON (f.id = l.film_id) " +
                            "JOIN mpa AS m ON m.id = f.mpa_id " +
                            "ORDER BY f.rate DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, friendId);

            ResultSet resultSet = preparedStatement.executeQuery();
            commonFilms.add(makeFilm(resultSet));
        } catch (SQLException e) {
            throw new RuntimeException("FilmDbStorage: getCommonFilms sql exception.");
        }
        return commonFilms;
    }

    public boolean remove(long filmId){
        try {
            String sql = "DELETE FROM film WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, filmId);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("FilmDbStorage: remove sql exception.");
        }
    }

    public List<Film> getMostPopularFilms(int count) {
        List<Film> mostPopularFilms = new ArrayList<>();
        try {
            String sql = "SELECT  film.id, film.name, description, release_date, duration, mpa_id, " +
                    "COUNT(l.FILM_ID) as rate, m.NAME as mpa_name " +
                    "FROM film " +
                    "LEFT JOIN likes AS l on film.id = l.film_id " +
                    "LEFT JOIN film_genre AS fg on film.id = fg.film_id " +
                    "LEFT JOIN mpa AS m on film.mpa_id = m.id " +
                    "GROUP BY  film.id " +
                    "ORDER BY rate DESC " +
                    "LIMIT ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, count);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                mostPopularFilms.add(makeFilm(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FilmDbStorage: getMostPopularFilms sql exception.");
        }
        return mostPopularFilms;
    }

    private static Film makeFilm(ResultSet resultSet) throws SQLException {
        Date releaseDate = resultSet.getDate("release_date");
        int mpaId = resultSet.getInt("mpa_id");
        String mpaName = resultSet.getString("mpa_name");
        return new Film(resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                Objects.isNull(releaseDate) ? null : releaseDate.toLocalDate(),
                resultSet.getInt("duration"),
                mpaId == 0 ? null : new Mpa(mpaId, mpaName),
                null,
                resultSet.getInt("rate"));
    }
}
