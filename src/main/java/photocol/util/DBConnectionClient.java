package photocol.util;


import java.sql.*;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
public class DBConnectionClient {
    private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    private static final String USER = "photo_server";
    private static final String PASS = System.getenv("DB_PASS");
    private static final String DB_URL = System.getenv("DB_URL");
    private static HikariDataSource DBCP;
    public DBConnectionClient() {
        try{
            DBCP = new HikariDataSource();
            DBCP.setDriverClassName(JDBC_DRIVER);
            DBCP.setJdbcUrl(DB_URL);
            DBCP.setUsername(USER);
            DBCP.setPassword(PASS);
            DBCP.setMinimumIdle(5);
            DBCP.setMaximumPoolSize(500);
            DBCP.setLoginTimeout(1);
            DBCP.setAutoCommit(true);
            DBCP.setConnectionTimeout(5000);
            DBCP.setIdleTimeout(120000);
        } catch (Exception err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
    }

    public static DataSource getDataSource() {
        return DBCP;
    }
}
