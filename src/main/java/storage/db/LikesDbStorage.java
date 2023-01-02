package storage.db;

import storage.LikesStorage;
import storage.SqlConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LikesDbStorage implements LikesStorage {

    private static Connection connection = SqlConnection.createConnection();

    @Override
    public boolean addLike(long filmId, long userId) {
        try {
            String sql = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, filmId);
            preparedStatement.setLong(2, userId);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("LikesDbStorage: addLike sql exception.");
        }
    }

    @Override
    public boolean removeLike(long filmId, long userId) {
        try {
            String sql = "DELETE FROM likes WHERE film_id=? AND user_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, filmId);
            preparedStatement.setLong(2, userId);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("LikesDbStorage: removeLike sql exception.");
        }
    }
}
