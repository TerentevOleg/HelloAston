package storage.db;

import model.User;
import storage.FriendsStorage;
import storage.SqlConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendsDbStorage implements FriendsStorage {

    private static Connection connection = SqlConnection.createConnection();

    @Override
    public boolean addFriend(long userId, long friendId) {
        try {
            String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, friendId);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("FriendsDbStorage: addFriend sql exception.");
        }
    }

    @Override
    public boolean removeFriend(long userId, long friendId) {
        try {
            String sql = "DELETE FROM friends WHERE user_id=? AND friend_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, friendId);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("FriendsDbStorage: removeFriend sql exception.");
        }

    }

    @Override
    public List<User> getFriends(long userId) {
        List<User> friends = new ArrayList<>();
        try {
            String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                    "FROM users AS u " +
                    "JOIN friends AS f ON u.id=f.friend_id " +
                    "WHERE f.user_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                friends.add(UserDbStorage.makeUser(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FriendsDbStorage: getFriends sql exception.");
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        List<User> commonFriends = new ArrayList<>();
        try {
            String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                    "FROM users AS u " +
                    "JOIN friends AS f1 ON u.id=f1.friend_id " +
                    "JOIN friends AS f2 ON f1.friend_id=f2.friend_id " +
                    "WHERE f1.user_id=? AND f2.user_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, otherId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                commonFriends.add(UserDbStorage.makeUser(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FriendsDbStorage: getCommonFriends sql exception.");
        }
        return commonFriends;
    }
}
