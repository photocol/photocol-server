package photocol.layer.DataBase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class dbprint {
    public void print(ResultSet rs) throws SQLException {
        try{
            while(rs.next()){
                for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
                    System.out.println(rs.getString(i)+"\t");
                }
            }
        }
        catch(SQLException ex){
            System.out.println("Print ResultSet Error in dbprint");
        }
    }
}
