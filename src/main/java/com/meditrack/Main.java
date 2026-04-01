package com.meditrack;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("MediTrack CLI System Starting...");

        try (Connection conn = DBConnection.getConnection()) {

            if (conn != null) {
                System.out.println("-------------------------------------------");
                System.out.println("SUCCESS: Java is now connected with the MySQL!");
                System.out.println("-------------------------------------------");
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Could not connect to the database.");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}