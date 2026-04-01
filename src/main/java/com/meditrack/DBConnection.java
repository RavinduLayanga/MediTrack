package com.meditrack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = System.getenv("db.url");
    private static final String USER = System.getenv("db.user");
    private static final String PASSWORD = System.getenv("db.password");

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        // Validation: check the environment variables are available
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("Database credentials not found in Environment Variables!");
        }

        if (connection == null || connection.isClosed()) {
            try {
                // Loading the MySQL Driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establishing the link
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database Connected Successfully!");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL Driver not found! Check pom.xml.");
                e.printStackTrace();
            }
        }
        return connection;
    }
}