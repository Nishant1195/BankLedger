package com.bankladger.models;

import java.time.LocalDateTime;

public class Account {
    private final int id;
    private final String name;
    private final double balance;
    private final LocalDateTime createdAt;

    public Account(int id, String name, double balance, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("Account[id=%d, name=%s, balance=%.2f]", id, name, balance);
    }
}
