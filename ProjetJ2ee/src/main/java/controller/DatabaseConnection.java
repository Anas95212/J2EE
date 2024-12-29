package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 

public class DatabaseConnection {
    private static final String URL = "mysql-zinee91.alwaysdata.net";
    private static final String USER = "zinee91_game";
    private static final String PASSWORD = "J2EE2025";
 
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

