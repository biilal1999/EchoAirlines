package com.github.satr.ask.handlers.Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;

public class Database {

    private String driver, url;
    private String database, hostname, port;
    private String username, password;
    private Connection conn;

    public Database() {
        this.driver = "com.mysql.cj.jdbc.Driver";
        this.hostname = System.getenv("DB_HOST");
        this.database = System.getenv("DB_NAME");
        this.port = System.getenv("DB_PORT");
        this.url = "jdbc:mysql:aws://" + hostname + ":" + port + "/" + database + "?useSSL=true";
        this.username = System.getenv("DB_USER");
        this.password = System.getenv("DB_PASSWORD");
        this.conn = null;
    }


    public Connection abrirConexion(){
        conn = null;

        try{
            //Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);

        } catch (SQLException e){
            e.printStackTrace();
        }

        return conn;
    }

    public void cerrarConexion(){
        try{
            conn.close();

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public ResultSet ejecutarConsulta(String consulta){
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement(consulta);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
             rs = st.executeQuery();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return rs;
    }

    public PreparedStatement ejecutarInsercion(String insercion){
        PreparedStatement st = null;

        try{
            st = conn.prepareStatement(insercion);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return st;
    }

}
