package com.bankladger.service;

import com.bankladger.db.DBConnection;
import com.bankladger.repository.AccountRepository;
import com.bankladger.util.TransactionLogger;
import java.sql.Connection;
import java.sql.SQLException;

public class BankService {
    private final AccountRepository accountRepo;
    private final TransactionLogger logger;

    public BankService(AccountRepository accountRepo, TransactionLogger logger) {
        this.accountRepo = accountRepo;
        this.logger = logger;
    }

    public void createAccount(String name) {
        Connection conn = null;
        try {
            conn = DBConnection.get();
            int accountId = accountRepo.createAccount(conn, name);
            conn.commit();
            System.out.println("Account created with ID: " + accountId);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            System.out.println("Failed to create account: " + e.getMessage());
        }
    }

    public void deposit(int accountId, double amount) {
        if (amount <= 0) {
            System.out.println("Deposit failed. Amount must be positive.");
            return;
        }
        Connection conn = null;
        try {
            conn = DBConnection.get();
            accountRepo.credit(conn, accountId, amount);
            accountRepo.logTransaction(conn, 0, accountId, amount, "DEPOSIT", "SUCCESS");
            conn.commit();
            logger.log(0, accountId, amount, "SUCCESS");
            System.out.printf("Successfully deposited %.2f to account %d%n", amount, accountId);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    public void withdraw(int accountId, double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal failed. Amount must be positive.");
            return;
        }
        Connection conn = null;
        try {
            conn = DBConnection.get();
            accountRepo.debit(conn, accountId, amount);
            accountRepo.logTransaction(conn, accountId, 0, amount, "WITHDRAWAL", "SUCCESS");
            conn.commit();
            logger.log(accountId, 0, amount, "SUCCESS");
            System.out.printf("Successfully withdrew %.2f from account %d%n", amount, accountId);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    public void transfer(int fromId, int toId, double amount) {
        if (amount <= 0) {
            System.out.println("Transfer failed. Amount must be positive.");
            return;
        }
        Connection conn = null;
        try {
            conn = DBConnection.get();
            // Debit from source account
            accountRepo.debit(conn, fromId, amount);
            // Credit to destination account (if this throws, the catch block will roll back the debit)
            accountRepo.credit(conn, toId, amount);
            // Log the successful database transaction
            accountRepo.logTransaction(conn, fromId, toId, amount, "TRANSFER", "SUCCESS");
            conn.commit();
            logger.log(fromId, toId, amount, "SUCCESS");
            System.out.printf("Successfully transferred %.2f from account %d to account %d%n", amount, fromId, toId);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.log(fromId, toId, amount, "ROLLED_BACK");
            System.out.println("Transfer failed. Transaction rolled back.");
        }
    }

    public void getBalance(int accountId) {
        Connection conn = null;
        try {
            conn = DBConnection.get();
            double balance = accountRepo.getBalance(conn, accountId);
            logger.cacheBalance(accountId, balance);
            System.out.printf("Account %d balance: %.2f%n", accountId, balance);
        } catch (Exception e) {
            System.out.println("Failed to get balance: " + e.getMessage());
        }
    }
}
