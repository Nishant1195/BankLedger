package com.bankladger.models;

import java.time.LocalDateTime;

public class TransactionRecord {
    private final int fromId;
    private final int toId;
    private final double amount;
    private final String status;
    private final LocalDateTime executedAt;

    public TransactionRecord(int fromId, int toId, double amount, String status, LocalDateTime executedAt) {
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
        this.status = status;
        this.executedAt = executedAt;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] From: %d -> To: %d | Amount: %.2f | %s", executedAt, fromId, toId, amount, status);
    }
}
