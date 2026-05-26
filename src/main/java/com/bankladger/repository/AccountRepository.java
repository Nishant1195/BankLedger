package com.bankladger.repository;

import com.bankladger.models.Account;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;

public class AccountRepository {

    public int createAccount(Connection conn, String name) throws SQLException {
        String sql = "INSERT INTO accounts (name, balance) VALUES (?, 0.00)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated account ID.");
                }
            }
        }
    }

    public double getBalance(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                } else {
                    throw new IllegalArgumentException("Account not found: " + accountId);
                }
            }
        }
    }

    public void debit(Connection conn, int accountId, double amount) throws SQLException {
        double currentBalance = getBalance(conn, accountId);
        if (currentBalance < amount) {
            throw new IllegalStateException("Insufficient funds: balance " + currentBalance);
        }
        String sql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    public void credit(Connection conn, int accountId, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    public Account getAccount(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT id, name, balance, created_at FROM accounts WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    double balance = rs.getDouble("balance");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    return new Account(id, name, balance, createdAt);
                } else {
                    throw new IllegalArgumentException("Account not found: " + accountId);
                }
            }
        }
    }

    public void logTransaction(Connection conn, int fromId, int toId, double amount, 
                               String type, String status) throws SQLException {
        String sql = "INSERT INTO transactions (from_account, to_account, amount, type, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (fromId == 0) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, fromId);
            }

            if (toId == 0) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, toId);
            }

            ps.setDouble(3, amount);
            ps.setString(4, type);
            ps.setString(5, status);
            ps.executeUpdate();
        }
    }
}
