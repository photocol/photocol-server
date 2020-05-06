package photocol.util;


import java.sql.*;

public class DBConnectionClient {
    private final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
//    private String DB_URL = "jdbc:mysql://127.0.0.1:6600/";
    private String DB_URL = "jdbc:mysql://localhost/";

    //  Database credentials
    /* TODO: move credentials to environment variables */
    private final String USER = "photo_server";
    private final String PASS = "password";
    private Connection connection = null;
    public DBConnectionClient() {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

            connection.prepareStatement("USE photocol").executeUpdate();
        } catch (Exception err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
