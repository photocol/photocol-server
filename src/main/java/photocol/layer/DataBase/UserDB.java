package photocol.layer.DataBase;

import photocol.layer.DataBase.Method.InitDB;
import photocol.layer.DataBase.Method.TableManage;

import java.sql.Connection;

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
    public void


}
