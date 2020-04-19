package photocol.layer.store;

import photocol.definitions.User;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.DataBase.Method.InitDB;

import java.sql.*;

import static photocol.definitions.exception.HttpMessageException.Error.*;
import static photocol.definitions.response.StatusResponse.Status.*;

public class UserStore {
    Connection conn = null;
    public UserStore(){
        conn = new InitDB().initialDB("photocol");
    }

    /**
     * Create a user in the database with the given details
     * @param user  User object to create
     * @return      Newly created user uid
     * @throws HttpMessageException on failure
     */
    public int createUser(User user) throws HttpMessageException {
        try {
            PreparedStatement stmt =
                    conn.prepareStatement("INSERT INTO user (email, username, password) VALUES(?,?,?);",
                            Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.email);
            stmt.setString(2, user.username);
            stmt.setString(3, user.passwordHash);

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt("uid");
        } catch(SQLIntegrityConstraintViolationException err) {
            // unique contraint violated (username not unique)
            throw new HttpMessageException(401, CREDENTIALS_NOT_UNIQUE);
        } catch (SQLException err) {
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
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM user WHERE username=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException err){
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
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid, password FROM user WHERE username=?");
            stmt.setString(1, user.username);
            ResultSet rs = stmt.executeQuery();

            // TODO: implement more advanced check w/ one-way hashing
            rs.next();
            if(rs.getString(2).equals(user.passwordHash))
                return rs.getInt("uid");

            throw new HttpMessageException(401, CREDENTIALS_INVALID);
        } catch(SQLException err) {
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
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM user WHERE username=?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                throw new HttpMessageException(401, USER_NOT_FOUND, username);

            return rs.getInt("uid");
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

}