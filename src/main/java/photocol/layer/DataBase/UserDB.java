package photocol.layer.DataBase;

import photocol.definitions.response.StatusResponse;
import photocol.layer.DataBase.Method.InitDB;
import photocol.layer.DataBase.Method.TableManage;

import java.sql.Connection;
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
    public void signIn(){

    }
    
    public StatusResponse.Status checkIfUserExists(String email) {
        try {
            //check if email is null
            //
            String query = "SELECT uid FROM users WHERE email =\'" + email + "\';";

            cmd = "DROP DATABASE IF EXISTS " + dbname + ";";
            ResultSet rs = stat.executeQuery(cmd);
        } catch (SQLException SER){

        } catch (Exception er){

        }
    }


}
