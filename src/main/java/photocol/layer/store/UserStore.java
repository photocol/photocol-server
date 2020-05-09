package photocol.layer.store;

import org.springframework.security.crypto.bcrypt.BCrypt;
import photocol.definitions.User;
import photocol.definitions.exception.HttpMessageException;
import photocol.util.DBConnectionClient;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static photocol.definitions.exception.HttpMessageException.Error.*;

public class UserStore {
    DataSource dbcp;
    public UserStore(DBConnectionClient dbClient) {
        dbcp = dbClient.getDataSource();
    }

    /**
     * Create a user in the database with the given details
     * @param user  User object to create
     * @return      Newly created user uid
     * @throws HttpMessageException on failure
     */
    public int createUser(User user) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt =
                    conn.prepareStatement("INSERT INTO user (email, username, password) VALUES(?,?,?);",
                            Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.email);
            stmt.setString(2, user.username);

            // hash/salt password using spring-security's crypto bcrypt class
            stmt.setString(3, BCrypt.hashpw(user.password, BCrypt.gensalt(12)));

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            int get = rs.getInt("uid");
            conn.close();
            return get;
        } catch(SQLIntegrityConstraintViolationException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
            // unique contraint violated (username not unique)
            throw new HttpMessageException(401, CREDENTIALS_NOT_UNIQUE);
        } catch (SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Check if user exists. Returns boolean.
     * @param username  Username to check existence of
     * @return          Whether user exists in database or not
     * @throws HttpMessageException on failure
     */
    public boolean checkIfUserExists(String username) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM user WHERE username=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            boolean next = rs.next();
            conn.close();
            return next;
        } catch (SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Check if username/password are correct
     * @param user  User object with username and password to verify
     * @return      uid on success
     * @throws HttpMessageException on failure or incorrect credentials
     */
    public int checkCredentials(User user) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid, password FROM user WHERE username=?");
            stmt.setString(1, user.username);
            ResultSet rs = stmt.executeQuery();

            if(!rs.next())
            {
                conn.close();
                throw new HttpMessageException(401, USER_NOT_FOUND);
            }

            if(BCrypt.checkpw(user.password, rs.getString("password")))
            {
                conn.close();
                return rs.getInt("uid");
            }
            conn.close();
            throw new HttpMessageException(401, CREDENTIALS_INVALID);
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Get the UID associated with a username
     * @param username  Username to get the uid of
     * @return          uid of user on success
     * @throws HttpMessageException on failure or user not found
     */
    public int getUid(String username) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM user WHERE username=?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
            {
                conn.close();
                throw new HttpMessageException(401, USER_NOT_FOUND, username);
            }
            int get = rs.getInt("uid");
            conn.close();
            return get;
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Search users matching query
     * @param query string query
     * @return      list of usernames matching query
     * @throws HttpMessageException on error
     */
    public List<String> searchUsers(String query) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT username FROM user " +
                    "WHERE username LIKE ? OR email LIKE ?");
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();

            List<String> usernameList = new ArrayList<>();
            while(rs.next())
                usernameList.add(rs.getString("username"));
            conn.close();
            return usernameList;
        } catch (SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

}