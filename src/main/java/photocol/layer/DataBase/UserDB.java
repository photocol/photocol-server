package photocol.layer.DataBase;

import photocol.definitions.response.StatusResponse;
import photocol.definitions.response.StatusResponse.Status;
import photocol.layer.DataBase.Method.InitDB;
import photocol.layer.DataBase.Method.TableManage;

import java.sql.*;

import static photocol.definitions.response.StatusResponse.Status.*;

public class UserDB {
    InitDB UDb = null;
    Connection conn = null;
    TableManage ureg = null;
    public UserDB(){
        UDb = new InitDB();
        conn = UDb.initialDB("USR");
        ureg = new TableManage(conn);
        ureg.addTable("USERTB","email VARCHAR(100) NOT NULL UNIQUE", "username VARCHAR(100) NOT NULL", "password VARCHAR(100)");
    }

    public StatusResponse<Integer> logIn(String email, String password){
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT password FROM USERTB WHERE email=?");
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();
            rs.next(); //iterator points to before the first result?
            if( password.equals(rs.getString("password")) ) {
                System.out.println(rs.getString("password"));
                return new StatusResponse<>(STATUS_LOGGED_IN, rs.getInt(1));
            }
        }
        catch (SQLException SER){
            System.out.println(SER);
            SER.printStackTrace();
        }
        catch (Exception er){
            er.printStackTrace();
        }
        return null;
    }
    public StatusResponse<Integer> signUp(String email, String username, String password){
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO USERTB (email, username, password) VALUES(?,?,?)");
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.executeUpdate();
            return new StatusResponse<>(STATUS_USER_CREATED, 12321321);
        } catch (Exception eer) {
            eer.printStackTrace();
            return new StatusResponse<>(STATUS_USER_NOT_CREATED);
        }
    }

    public StatusResponse checkIfUserExists(String email) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM USERTB WHERE email=?");
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();

            return new StatusResponse(rs.next() ? STATUS_USER_FOUND : STATUS_USER_NOT_FOUND);
        } catch (Exception err){
            err.printStackTrace();
            return new StatusResponse(STATUS_USER_NOT_FOUND);
        }
    }


}