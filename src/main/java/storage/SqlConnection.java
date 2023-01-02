package storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConnection {
    /*private static final String URL = "jdbc:h2:file:./db/helloaston";
    private static final String USER = "sa";
    private static final String PASSWORD = "password";*/

    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
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
