package storage.db;

import model.Mpa;
import storage.MpaStorage;
import storage.SqlConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MpaDbStorage implements MpaStorage {

    private static Connection connection = SqlConnection.createConnection();

    @Override
    public List<Mpa> getAll() {
        List<Mpa> mpas = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            String sql = "SELECT id, name FROM mpa";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                mpas.add(makeMpa(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("MpaDbStorage: getAll sql exception.");
        }
        return mpas;
    }

    @Override
    public Optional<Mpa> getById(long id) {
        Mpa mpa = null;
        try {
            String sql = "SELECT id, name FROM mpa WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            mpa = makeMpa(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException("MpaDbStorage: getById sql exception.");
        }

        return Optional.of(mpa);
    }

    private static Mpa makeMpa(ResultSet resultSet) throws SQLException {
        return new Mpa(resultSet.getInt("id"), resultSet.getString("name"));
    }
}
