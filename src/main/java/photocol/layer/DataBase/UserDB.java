package photocol.layer.DataBase;

import photocol.definitions.response.StatusResponse;
import photocol.definitions.response.StatusResponse.Status;
import photocol.layer.DataBase.Method.InitDB;
import photocol.layer.DataBase.Method.TableManage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static photocol.definitions.response.StatusResponse.Status.STATUS_USER_CREATED;
import static photocol.definitions.response.StatusResponse.Status.STATUS_USER_NOT_CREATED;

public class UserDB {
    InitDB UDb = null;
    Connection conn = null;
    TableManage ureg = null;
    public UserDB(){
        UDb = new InitDB();
        conn = UDb.initialDB("USR");
        ureg = new TableManage(conn);
        ureg.addTable("USERTB","email VARCHAR(100) NOT NULL UNIQUE", "username VARCHAR(100) NOT NULL", "password VARCHAR(100)");
    }

    public Status logIn(String email, String password){
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT password FROM USERTB WHERE email=?");
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();

            //next we check if the result matches
            try{
                while(rs.next()){
                    for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
                        System.out.print(rs.getString(i)+"\t");
                    }
                    System.out.println("");
                }
            }
            catch(SQLException ex){
                System.out.println("Print ResultSet Error in dbprint");
            }
        }
        catch (SQLException SER){
            System.out.println(SER);
            SER.printStackTrace();
        }
        catch (Exception er){
            er.printStackTrace();
        }
        return null;
    }
    public StatusResponse<Integer> signUp(String email, String username, String password){
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO USERTB (email, username, password) VALUES(?,?,?)");
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.executeUpdate();
            return new StatusResponse<>(STATUS_USER_CREATED, 12321321);
        }
        catch (SQLException SER) {
            System.out.println(SER);
            SER.printStackTrace();
            return new StatusResponse<>(STATUS_USER_NOT_CREATED);
        }
        catch (Exception er) {
            er.printStackTrace();
            return new StatusResponse<>(STATUS_USER_NOT_CREATED);
        }
    }

    public Status checkIfUserExists(String email) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM USERTB WHERE email=?");
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
            SER.printStackTrace();
        }
        catch (Exception er){
            er.printStackTrace();
        }
        return null;
    }


}