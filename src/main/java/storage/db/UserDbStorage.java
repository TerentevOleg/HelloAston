package storage.db;

import model.User;
import storage.SqlConnection;
import storage.UserStorage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserDbStorage implements UserStorage {

    private static Connection connection = SqlConnection.createConnection();

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        try {
            String sql = "SELECT id, email, login, name, birthday FROM users";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                users.add(makeUser(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDbStorage: getUsers sql exception.");
        }
        return users;
    }

    @Override
    public Optional<User> getById(long id) {
        User user = null;
        try {
            String sql = "SELECT id, email, login, name, birthday FROM users WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                user = makeUser(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDbStorage: getById sql exception.");
        }
        return Optional.ofNullable(user);
    }

    @Override
    public long add(User user) {
        try {
            String sql = "INSERT INTO users(email, login, name, birthday) VALUES(?, ?, ?, ?)";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(sql);

            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getLogin());
            preparedStatement.setString(3, user.getName());
            preparedStatement.setDate(4, Date.valueOf(user.getBirthday()));

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("UserDbStorage: add sql exception.");
        }
    }

    @Override
    public boolean update(User user) {
        try {
            String sql = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getLogin());
            preparedStatement.setString(3, user.getName());
            preparedStatement.setDate(4, Date.valueOf(user.getBirthday()));
            preparedStatement.setLong(5, user.getId());

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("UserDbStorage: update sql exception.");
        }
    }

    @Override
    public boolean remove(long userId) {
        try {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, userId);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("UserDbStorage: remove sql exception.");
        }
    }

    public static User makeUser(ResultSet resultSet) throws SQLException {
        Date birthday = resultSet.getDate("birthday");
        return new User(resultSet.getLong("id"),
                resultSet.getString("email"),
                resultSet.getString("login"),
                resultSet.getString("name"),
                Objects.isNull(birthday) ? null : birthday.toLocalDate());
    }
}
