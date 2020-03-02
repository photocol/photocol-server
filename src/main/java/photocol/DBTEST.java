package photocol;
import photocol.layer.DataBase.Method.InitDB;
import photocol.layer.DataBase.Method.TableManage;
import photocol.layer.DataBase.UserDB;

import javax.swing.text.TabableView;
import java.sql.*;
public class DBTEST {
    public static void main(String[] Args) throws SQLException {
        UserDB udb = new UserDB();
        udb.signUp("victorzh716@gmail.com", "victooor", "pw123");
        udb.logIn("victorzh716@gmail.com","pw123");


    }
}
