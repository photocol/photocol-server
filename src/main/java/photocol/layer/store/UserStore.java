package photocol.layer.store;

import photocol.definitions.response.StatusResponse;
import photocol.layer.DataBase.Method.InitDB;
import photocol.layer.DataBase.Method.TableManage;

import java.sql.*;

import static photocol.definitions.response.StatusResponse.Status.*;

public class UserStore {
    Connection conn = null;
    public UserStore(){
        conn = new InitDB().initialDB("photocol");
    }

    public StatusResponse<Integer> createUser(String email, String username, String password){
        try {
            PreparedStatement stmt =
                    conn.prepareStatement("INSERT INTO user (email, username, password) VALUES(?,?,?);",
                                          Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.setString(3, password);

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return new StatusResponse<>(STATUS_OK, rs.getInt(1));
        } catch (Exception err) {
            err.printStackTrace();
            return new StatusResponse<>(STATUS_USER_NOT_CREATED);
        }
    }

    public StatusResponse checkIfUserExists(String username) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM user WHERE username=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            return new StatusResponse(rs.next() ? STATUS_OK : STATUS_USER_NOT_FOUND);
        } catch (Exception err){
            err.printStackTrace();
            return new StatusResponse(STATUS_USER_NOT_FOUND);
        }
    }

    public StatusResponse<Integer> checkCredentials(String username, String password) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid, password FROM user WHERE username=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            // TODO: implement more advanced check w/ one-way hashing
            rs.next();
            if(rs.getString(2).equals(password))
                return new StatusResponse<>(STATUS_OK, rs.getInt(1));

            return new StatusResponse<>(STATUS_CREDENTIALS_INVALID);
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse<>(STATUS_CREDENTIALS_INVALID);
        }
    }

    public StatusResponse<Integer> getUid(String username) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM user WHERE username=?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                return new StatusResponse<>(STATUS_USER_NOT_FOUND);

            return new StatusResponse<>(STATUS_OK, rs.getInt("uid"));
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse<>(STATUS_USER_NOT_FOUND);
        }
    }

}