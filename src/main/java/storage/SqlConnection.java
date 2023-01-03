package storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConnection {

    private static final String URL =
            "jdbc:tc:postgresql:15:///localhost:5432/postgres?TC_INITSCRIPT=file:src/test/resources/BD_SCRIPT.sql";
    private static final String USER = "postgres";
    private static final String PASSWORD = "iamroot";

    public static Connection createConnection() {

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver found.");

        } catch(ClassNotFoundException e) {
            System.out.println("Driver not found. " + e.getMessage());
        }

        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database.");
            return connection;

        } catch(SQLException e) {
            System.out.println("Not connected to database." + e.getMessage());
            return null;
        }
    }
}
