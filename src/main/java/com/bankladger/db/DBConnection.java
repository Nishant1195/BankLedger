package com.bankladger.db;

import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    // Single connection — not thread-safe by design. CLI demo only.
    private static Connection connection;

    private DBConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection get() throws SQLException {
        boolean closed = true;
        if (connection != null) {
            try {
                closed = connection.isClosed();
            } catch (SQLException e) {
                // Connection might be broken/invalid, treat as closed to recreate
                closed = true;
            }
        }
        if (connection == null || closed) {
            Properties props = new Properties();
            try (InputStream input = DBConnection.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties")) {
                if (input == null) {
                    throw new RuntimeException("config.properties not found in classpath");
                }
                props.load(input);
            } catch (IOException e) {
                throw new SQLException("Failed to load database configuration", e);
            }
            connection = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
            );
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        }
        return connection;
    }
}
