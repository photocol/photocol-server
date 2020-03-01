package photocol.layer.DataBase.Method;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TableManage {
    Connection conk=null;
    public TableManage(Connection input){
        this.conk = input;
    }
    //addTable with default id not null icrement
    public void addTable(String Tname, String ... column /*string must follow %DATANAME %DATATYPE format*/ ){

        try {
            String  cmd = "CREATE TABLE IF NOT EXISTS "+Tname+"(id NOT NULL AUTO_INCREMENT";
            for (String i : column) {
                cmd = cmd+","+i;
            }
            cmd = cmd + ");";
            Statement stat = this.conk.createStatement();
            stat.executeQuery(cmd);
        }
        catch (SQLException ser){

        }
    }
}
