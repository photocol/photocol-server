package photocol.layer.DataBase;

import java.sql.*;

public class InitDB {
    private String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    String DB_URL = "jdbc:mysql://localhost/";

    //  Database credentials
    private String USER = "root";
    private String PASS = "password";
    private Connection conk = null;
    public InitDB(){
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            this.conk = DriverManager.getConnection(DB_URL,USER,PASS);
        }
        catch (SQLException ser){
            System.out.println("InitDb Connection ERROR");
            System.out.println(ser.getMessage());
        }
        catch (Exception er){
            System.out.println("InitDb DriverDef ERROR");
            System.out.println(er.getMessage());
        }

    }
    public InitDB(String locale){
        this.DB_URL = locale;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            this.conk = DriverManager.getConnection(DB_URL,USER,PASS);
        }
        catch (SQLException ser){
            System.out.println("InitDb Connection ERROR");
            System.out.println(ser.getMessage());
        }
        catch (Exception er){
            System.out.println("InitDb DriverDef ERROR");
            System.out.println(er.getMessage());
        }


    }

    //can also be used to add/switch databases
    public Connection initDB(String dbname){
        try{
            Statement stat = conk.createStatement();
            String cmd;
            cmd = "CREATE DATABASE IF NOT EXISTS " + dbname + ";";
            ResultSet rs = stat.executeQuery(cmd);
            cmd = "USE " + dbname;
        } catch (SQLException SER){

        } catch (Exception er){

        }
        return this.conk;
    }
    public Connection addDb(String dbname){
        return this.initDB(dbname);

    }
    public Connection switchDb(String dbname){
        return this.initDB(dbname);
    }
    private void deleteDb(String dbname){
        try{
            Statement stat = conk.createStatement();
            String cmd;
            cmd = "DROP DATABASE IF EXISTS " + dbname + ";";
            ResultSet rs = stat.executeQuery(cmd);
        } catch (SQLException SER){

        } catch (Exception er){

        }
    }
}
