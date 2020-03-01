package photocol.layer.DataBase.Method;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TableManage {
    Connection conk=null;
    public TableManage(Connection input){
        this.conk = input;
    }
    //addTable with default id not null increment
    public void addTable(String Tname, String ... column /*string must follow %DATANAME %DATATYPE format*/ ){

        try {
            String  cmd = "CREATE TABLE IF NOT EXISTS "+Tname+"(id INT NOT NULL AUTO_INCREMENT";
            for (String i : column) {
                cmd = cmd+","+i;
            }
            cmd = cmd + ", PRIMARY KEY(id));";
            Statement stat = this.conk.createStatement();
            stat.executeQuery(cmd);
        }
        catch (SQLException ser){
            System.out.print("addTable ERROR");
        }
    }
    public void addTable(String Tname, int prime, String ... column /*string must follow %DATANAME %DATATYPE format*/ ){

        try {
            String  cmd = "CREATE TABLE IF NOT EXISTS "+Tname+"(";
            for (String i : column) {
                cmd = cmd+i+",";
            }
            cmd = cmd + " PRIMARY KEY(";
            for(int i =0; i < prime-1; i++){
                cmd = cmd + column[i].split(" ")[0] + ",";
            }
            cmd = cmd + column[prime-1].split(" ")[0]+"));";
            Statement stat = this.conk.createStatement();
            stat.executeQuery(cmd);
        }
        catch (SQLException ser){
            System.out.print("addTable ERROR");
        }
    }
    public int insert(String Tname, String valtemp /*FORMAT: val1, val2,val3*/, String ... values){
        String cmd  = "INSERT INTO "+Tname+" ("+valtemp+") VALUES (";
        for(String i:values){
            cmd = cmd+i+",";
        }
        cmd = cmd+");";
        System.out.println(cmd);
        return 0;
    }
}
