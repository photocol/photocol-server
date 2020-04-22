package photocol.layer.DataBase.Method;


import java.sql.*;

public class InitDB {
    private final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    private String DB_URL = "jdbc:mysql://127.0.0.1:6600/";

    //  Database credentials
    private final String USER = "photo_server";
    private final String PASS = "password";
    private Connection conk = null;
    public InitDB(){
        try {
            Class.forName(JDBC_DRIVER);
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
            Class.forName(JDBC_DRIVER);
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
    public Connection initialDB(String dbname){
        try{
            Statement stat = conk.createStatement();
            String cmd;
            cmd = "CREATE DATABASE IF NOT EXISTS " + dbname + ";";
            ResultSet rs = stat.executeQuery(cmd);
            cmd = "USE " + dbname;
            rs = stat.executeQuery(cmd);
        } catch (SQLException err){
            err.printStackTrace();
        } catch (Exception err){
            err.printStackTrace();
        }
        return this.conk;
    }
    public Connection addDb(String dbname){
        return this.initialDB(dbname);

    }
    public Connection switchDb(String dbname){
        return this.initialDB(dbname);
    }
    private void deleteDb(String dbname){
        try{
            Statement stat = conk.createStatement();
            String cmd;
            cmd = "DROP DATABASE IF EXISTS " + dbname + ";";
            ResultSet rs = stat.executeQuery(cmd);
        } catch (SQLException err){
            err.printStackTrace();
        } catch (Exception err){
            err.printStackTrace();
        }
    }
}
