package photocol.layer.DataBase;

import photocol.definitions.response.StatusResponse.Status;
import photocol.layer.DataBase.Method.InitDB;
import photocol.layer.DataBase.Method.TableManage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDB {
    InitDB UDb = null;
    Connection conn = null;
    TableManage ureg = null;
    public UserDB(){
        UDb = new InitDB();
        conn = UDb.initialDB("USR");
        ureg = new TableManage(conn);
        ureg.addTable("UseRgis","email VARCHAR(255) NOT NULL UNIQUE", "username VARCHAR(255) NOT NULL", "password VARCHAR(255)");
    }
    public Status logIn(String email, String password){
        return null;
    }
    public Status signUp(String email, String username, String password){
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (email, username, password) VALUES(?,?,?)");
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.setString(3, password);
            return Status.STATUS_USER_CREATED;
        }
        catch (SQLException SER) {
            System.out.println(SER);
            return Status.STATUS_USER_NOT_CREATED;
        }
        catch (Exception er) {

        }
        return null;
    }

    public Status checkIfUserExists(String email) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM users WHERE email=?");
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();

            //next we check if rs has any contents
            if(rs.next() == false) {
                System.out.println("DEBUG: No User");
                return Status.STATUS_USER_NOT_FOUND;
            }
            else {
                System.out.println("DEBUG: User Found");
                return Status.STATUS_USER_FOUND;
            }

        }
        catch (SQLException SER){
            System.out.println(SER);
        }
        catch (Exception er){

        }
        return null;
    }


}