package com.bankladger.util;

import com.bankladger.models.TransactionRecord;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class TransactionLogger {
    private final ArrayList<TransactionRecord> history = new ArrayList<>();
    private final HashMap<Integer, Double> accountBalanceCache = new HashMap<>();

    public void log(int fromId, int toId, double amount, String status) {
        history.add(new TransactionRecord(fromId, toId, amount, status, LocalDateTime.now()));
    }

    public void cacheBalance(int accountId, double balance) {
        accountBalanceCache.put(accountId, balance);
    }

    public Optional<Double> getCachedBalance(int accountId) {
        return Optional.ofNullable(accountBalanceCache.get(accountId));
    }

    public void printHistory() {
        if (history.isEmpty()) {
            System.out.println("No transactions this session.");
            return;
        }
        history.stream()
               .filter(record -> "SUCCESS".equalsIgnoreCase(record.getStatus()))
               .forEach(System.out::println);
    }

    public void printAllHistory() {
        if (history.isEmpty()) {
            System.out.println("No transactions this session.");
            return;
        }
        history.forEach(System.out::println);
    }
}
