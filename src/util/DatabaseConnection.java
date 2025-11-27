/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized database connection helper used by the RedStone project.
 * Replace the USER and PASSWORD constants with your MySQL credentials.
 * Make sure the MySQL JDBC driver (mysql-connector-java) is on the classpath.
 */
public class DatabaseConnection {
    private static Connection connection = null;

    // Update these values if your DB is different
    private static final String URL = "jdbc:mysql://localhost:3306/redstone?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // <-- change this
    private static final String PASSWORD = ""; // <-- change this

    private DatabaseConnection() {
        // private constructor to prevent instantiation
    }

    /**
     * Returns a singleton Connection instance. Call DatabaseConnection.getConnection()
     * from anywhere in the project. This method will load the MySQL JDBC driver
     * if necessary and open a new connection if the previous one was closed.
     *
     * @return open java.sql.Connection
     * @throws SQLException when connection fails
     */
    public static synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // ensure driver is available
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    // try older driver classname as fallback
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                    } catch (ClassNotFoundException ex) {
                        throw new SQLException("MySQL JDBC driver not found. Add the driver to the classpath.", ex);
                    }
                }

                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connected to database: " + URL);
            }
            return connection;
        } catch (SQLException e) {
            // surface clear message for debugging
            throw new SQLException("Failed to connect to database at " + URL + ": " + e.getMessage(), e);
        }
    }

    /**
     * Close the shared connection (optional). Safe to call multiple times.
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("🔒 Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("❌ Error closing database: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Quick manual test (optional). Run this class directly from an IDE to verify
     * the connection settings before running the whole application.
     */
    public static void main(String[] args) {
        try {
            Connection c = DatabaseConnection.getConnection();
            System.out.println("Connection OK: " + (c != null && !c.isClosed()));
            DatabaseConnection.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
