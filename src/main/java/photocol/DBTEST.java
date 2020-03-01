package photocol;
import photocol.layer.DataBase.Method.InitDB;
import photocol.layer.DataBase.Method.TableManage;
import photocol.layer.DataBase.UserDB;

import javax.swing.text.TabableView;
import java.sql.*;
public class DBTEST {
    public static void main(String[] Args) throws SQLException {
        InitDB x = new InitDB();
        Connection passthrough = x.initialDB("data");
        TableManage y = new TableManage(passthrough);
        y.addTable("user", "name VARCHAR(20)", "email VARCHAR(50)", "password VARCHAR(20)");
        y.addTable("collection", 2,"id INT NOT NULL","name VARCHAR(255) NOT NULL","user VARCHAR(255) NOT NULL");
        UserDB udb = new UserDB();
        

    }
}
