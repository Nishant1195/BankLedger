package com.bankladger;

import com.bankladger.repository.AccountRepository;
import com.bankladger.service.BankService;
import com.bankladger.util.TransactionLogger;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AccountRepository accountRepo = new AccountRepository();
        TransactionLogger logger = new TransactionLogger();
        BankService bankService = new BankService(accountRepo, logger);

        System.out.println("=== BankLedger: ACID Transaction System ===");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println();
                System.out.println("1. Create account");
                System.out.println("2. Deposit funds");
                System.out.println("3. Withdraw funds");
                System.out.println("4. Transfer funds");
                System.out.println("5. View balance");
                System.out.println("6. View history");
                System.out.println("7. Exit");
                System.out.print("Select an option: ");

                String optionStr = scanner.nextLine().trim();
                int option;
                try {
                    option = Integer.parseInt(optionStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, try again.");
                    continue;
                }

                if (option == 7) {
                    System.out.println("Goodbye.");
                    break;
                }

                try {
                    switch (option) {
                        case 1:
                            System.out.print("Enter account name: ");
                            String name = scanner.nextLine().trim();
                            bankService.createAccount(name);
                            break;
                        case 2:
                            System.out.print("Enter account ID: ");
                            int depAccountId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Enter amount: ");
                            double depAmount = Double.parseDouble(scanner.nextLine().trim());
                            bankService.deposit(depAccountId, depAmount);
                            break;
                        case 3:
                            System.out.print("Enter account ID: ");
                            int witAccountId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Enter amount: ");
                            double witAmount = Double.parseDouble(scanner.nextLine().trim());
                            bankService.withdraw(witAccountId, witAmount);
                            break;
                        case 4:
                            System.out.print("Enter source account ID: ");
                            int fromId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Enter destination account ID: ");
                            int toId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Enter amount: ");
                            double amount = Double.parseDouble(scanner.nextLine().trim());
                            bankService.transfer(fromId, toId, amount);
                            break;
                        case 5:
                            System.out.print("Enter account ID: ");
                            int balAccountId = Integer.parseInt(scanner.nextLine().trim());
                            bankService.getBalance(balAccountId);
                            break;
                        case 6:
                            logger.printHistory();
                            break;
                        default:
                            System.out.println("Invalid option, try again.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, try again.");
                }
            }
        }
    }
}
