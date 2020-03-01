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
        ureg.addTable("UseRgis","username VARCHAR(255) NOT NULL","email VARCHAR(255) NOT NULL UNIQUE", "password VARCHAR(255)");
    }
    public void LogIn(){

    }
    public Status signUp(String username, String password, String email){
//        PreparedStatement stmt = "INSERT INTO users values";
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

        } catch (SQLException SER){
            System.out.println(SER);
        } catch (Exception er){

        }
        return null;
    }


}
