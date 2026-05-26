package com.bankladger.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
            connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/bankladger",
                "postgres",
                "postgres"
            );
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        }
        return connection;
    }
}
