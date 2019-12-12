package sample;

import java.sql.*;

public class JdbcConnection {
    String url;
    String user;
    String pass;
    Connection con;


    JdbcConnection(String user,String pass,String ip) throws SQLException {
        this.url = "jdbc:mysql://"+ ip +":3306/";
        this.user = user;
        this.pass = pass;
    }

    public Connection getConnection(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            con = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            System.out.println("could not connect");
            e.printStackTrace();
        }
        return con;
    }
}
