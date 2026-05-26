# Project Completion Document

## 1. Project Summary
BankLedger is a command-line interface (CLI) banking application built in Java to demonstrate ACID transaction properties using raw JDBC and a PostgreSQL 15 database. The project does not rely on any high-level ORM frameworks (like Hibernate) or application frameworks (like Spring), ensuring that database connections, statement execution, and transaction boundaries (commit/rollback) are explicitly controlled via code. The application supports core financial functions including account creation, deposits, withdrawals, transfers, balance queries, and session transaction logging.

## 2. File Inventory

| File | Package/Path | One-Sentence Description |
| :--- | :--- | :--- |
| `Main.java` | `src/main/java/com/bankladger/Main.java` | Serves as the interactive CLI application entry point and command processing loop for user input. |
| `DBConnection.java` | `src/main/java/com/bankladger/db/DBConnection.java` | Manages a singleton JDBC connection configured with manual commits and serializable transaction isolation. |
| `Account.java` | `src/main/java/com/bankladger/models/Account.java` | Represents a simple, immutable domain object (POJO) for a bank account. |
| `TransactionRecord.java` | `src/main/java/com/bankladger/models/TransactionRecord.java` | Represents an immutable domain object (POJO) for an in-memory session transaction log record. |
| `AccountRepository.java` | `src/main/java/com/bankladger/repository/AccountRepository.java` | Performs direct, low-level SQL operations using prepared statements to query, update, and insert account and transaction data. |
| `BankService.java` | `src/main/java/com/bankladger/service/BankService.java` | Exposes high-level banking operations and orchestrates transaction boundaries by explicitly invoking commit or rollback. |
| `TransactionLogger.java` | `src/main/java/com/bankladger/util/TransactionLogger.java` | Caches queried balances in a `HashMap` and appends successful transaction records to an `ArrayList` session history. |
| `schema.sql` | `schema.sql` | Defines the database schema for the `accounts` and `transactions` tables, including a non-negative balance check constraint. |
| `docker-compose.yml` | `docker-compose.yml` | Configures and runs the PostgreSQL 15 container with host port binding and a persistent volume for database data. |
| `pom.xml` | `pom.xml` | Configures the Maven build settings, targeting Java 17 with the PostgreSQL JDBC dependency and assembly packaging. |

## 3. ACID Implementation Evidence

* **Atomicity**
  * **File**: `src/main/java/com/bankladger/service/BankService.java`
  * **Method Name**: `transfer()`
  * **Mechanism**: The method executes `debit()` and `credit()` operations sequentially on the same database connection and invokes `conn.rollback()` in the `catch` block on any exception to ensure that either both modifications succeed or neither does.
* **Consistency**
  * **File**: `src/main/java/com/bankladger/repository/AccountRepository.java` (and `schema.sql`)
  * **Method Name**: `debit()` (and DB CHECK constraint `chk_balance_non_negative`)
  * **Mechanism**: The database table enforces non-negative balances using the `chk_balance_non_negative` check constraint, while the application pre-validates in the `debit` method by throwing an `IllegalStateException` if the current balance is less than the debit amount.
* **Isolation**
  * **File**: `src/main/java/com/bankladger/db/DBConnection.java`
  * **Method Name**: `get()`
  * **Mechanism**: The singleton connection returned by the `get` method is configured with `connection.setAutoCommit(false)` and `connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE)` to enforce the strictest isolation level, preventing all concurrent anomalies.
* **Durability**
  * **File**: `src/main/java/com/bankladger/service/BankService.java` (and `docker-compose.yml`)
  * **Method Name**: `transfer()` (specifically calling `conn.commit()`)
  * **Mechanism**: The invocation of `conn.commit()` instructs PostgreSQL to flush the transactional changes to its write-ahead log (WAL), which persists to disk on the host-mapped `pgdata` volume configured in `docker-compose.yml`.

## 4. Schema
The database schema defined in `schema.sql` is as follows:
```sql
CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    from_account INT REFERENCES accounts(id),
    to_account INT REFERENCES accounts(id),
    amount NUMERIC(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    executed_at TIMESTAMP DEFAULT NOW()
);
```

## 5. Build Verification
Running the Maven build with Java 21 compile configurations while targeting Java 17 compiles the code and generates the assembly JAR successfully.

**Build Command:**
```bash
JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64 mvn clean package
```

**Full Build Output:**
```text
[INFO] Scanning for projects...
[INFO] 
[INFO] ---------------------< com.bankladger:BankLedger >----------------------
[INFO] Building BankLedger 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ BankLedger ---
[INFO] Deleting /home/nishant/Projects/BankLedger/target
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ BankLedger ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/nishant/Projects/BankLedger/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ BankLedger ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 7 source files to /home/nishant/Projects/BankLedger/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ BankLedger ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/nishant/Projects/BankLedger/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ BankLedger ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ BankLedger ---
[INFO] No tests to run.
[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ BankLedger ---
[INFO] Building jar: /home/nishant/Projects/BankLedger/target/BankLedger-1.0-SNAPSHOT.jar
[INFO] 
[INFO] --- maven-assembly-plugin:3.6.0:single (make-assembly) @ BankLedger ---
[INFO] Building jar: /home/nishant/Projects/BankLedger/target/BankLedger-1.0-SNAPSHOT-jar-with-dependencies.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.277 s
[INFO] Finished at: 2026-05-26T12:37:50+05:30
[INFO] ------------------------------------------------------------------------
```

## 6. End-to-End Test Results

### Verified Test Sequence Output
```text
=== BankLedger: ACID Transaction System ===

1. Create account
2. Deposit funds
3. Withdraw funds
4. Transfer funds
5. View balance
6. View history
7. Exit
Select an option: 1
Enter account name: Charlie
Account created with ID: 3

1. Create account
2. Deposit funds
3. Withdraw funds
4. Transfer funds
5. View balance
6. View history
7. Exit
Select an option: 2
Enter account ID: 3
Enter amount: 500
Successfully deposited 500.00 to account 3

1. Create account
2. Deposit funds
3. Withdraw funds
4. Transfer funds
5. View balance
6. View history
7. Exit
Select an option: 4
Enter source account ID: 3
Enter destination account ID: 1
Enter amount: 200
Successfully transferred 200.00 from account 3 to account 1

1. Create account
2. Deposit funds
3. Withdraw funds
4. Transfer funds
5. View balance
6. View history
7. Exit
Select an option: 4
Enter source account ID: 2
Enter destination account ID: 1
Enter amount: 1000
Transfer failed. Transaction rolled back.

1. Create account
2. Deposit funds
3. Withdraw funds
4. Transfer funds
5. View balance
6. View history
7. Exit
Select an option: 5
Enter account ID: 3
Account 3 balance: 300.00

1. Create account
2. Deposit funds
3. Withdraw funds
4. Transfer funds
5. View balance
6. View history
7. Exit
Select an option: 5
Enter account ID: 2
Account 2 balance: 400.00

1. Create account
2. Deposit funds
3. Withdraw funds
4. Transfer funds
5. View balance
6. View history
7. Exit
Select an option: 5
Enter account ID: 1
Account 1 balance: 800.00

1. Create account
2. Deposit funds
3. Withdraw funds
4. Transfer funds
5. View balance
6. View history
7. Exit
Select an option: 6
[2026-05-26T12:38:23.016022230] From: 0 -> To: 3 | Amount: 500.00 | SUCCESS
[2026-05-26T12:38:23.021505428] From: 3 -> To: 1 | Amount: 200.00 | SUCCESS

1. Create account
2. Deposit funds
3. Withdraw funds
4. Transfer funds
5. View balance
6. View history
7. Exit
Select an option: 7
Goodbye.
```

### Durability Check Post-Container Restart
To verify durability, the PostgreSQL database container was shut down and restarted, and the data was queried to check persistence.

**1. Accounts state before database restart:**
```text
 id |  name   | balance |         created_at         
----+---------+---------+----------------------------
  2 | Bob     |  400.00 | 2026-05-26 12:18:57.733449
  3 | Charlie |  300.00 | 2026-05-26 12:38:22.991169
  1 | Alice   |  800.00 | 2026-05-26 12:18:41.797282
(3 rows)
```

**2. Stop and restart container command:**
```bash
docker compose stop && docker compose up -d
```

**3. Accounts state after database restart (persisted correctly):**
```text
 id |  name   | balance |         created_at         
----+---------+---------+----------------------------
  2 | Bob     |  400.00 | 2026-05-26 12:18:57.733449
  3 | Charlie |  300.00 | 2026-05-26 12:38:22.991169
  1 | Alice   |  800.00 | 2026-05-26 12:18:41.797282
(3 rows)
```

## 7. Known Limitations
The following limitations are defined in the project:
* Single JDBC connection — no connection pool. Not suitable for concurrent use.
* CLI only — no REST API layer.
* No unit tests — ACID properties verified through manual end-to-end testing.
* SERIALIZABLE isolation increases transaction abort rate under high concurrency — acceptable for a demo, needs tuning for production.
* Database credentials managed in `config.properties` (excluded via `.gitignore`) — `config.properties.example` provided as a template.

