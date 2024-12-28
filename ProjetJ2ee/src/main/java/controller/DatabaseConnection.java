package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bdd_J2EE";
    private static final String USER = "root";
    private static final String PASSWORD = "cytech0001";
 
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

