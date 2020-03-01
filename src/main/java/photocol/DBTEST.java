package photocol;
import photocol.layer.DataBase.Method.InitDB;
import photocol.layer.DataBase.Method.TableManage;

import javax.swing.text.TabableView;
import java.sql.*;
public class DBTEST {
    public static void main(String[] Args){
        InitDB x = new InitDB();
        Connection passthrough = x.initialDB("data");

    }
}
