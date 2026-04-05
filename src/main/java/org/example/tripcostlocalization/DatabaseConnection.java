package org.example.tripcostlocalization;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL =
            System.getenv().getOrDefault("TRIP_DB_URL", "jdbc:mariadb://localhost:3306/fuel_calculator_localization");
    private static final String USER = System.getenv().getOrDefault("TRIP_DB_USER", "myadmin");
    private static final String PASSWORD = System.getenv().getOrDefault("TRIP_DB_PASSWORD", "your_strong_password");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
