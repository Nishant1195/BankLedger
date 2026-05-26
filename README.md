# BankLedger

## 1. What This Is
CLI banking demo built to demonstrate ACID transaction properties using raw JDBC and PostgreSQL. No Spring, no Hibernate, no ORM — every SQL statement and every commit/rollback is explicit in the code.

## 2. Tech Stack
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core programming language for application logic |
| JDBC | standard library | Database connectivity and manual transaction boundary control |
| PostgreSQL | 15 | Relational database to persist accounts and transaction logs |
| Docker Compose | latest | Container orchestration to run the PostgreSQL database |
| Maven | 3.8+ | Dependency management and build packaging tool |

## 3. How to Run
```bash
git clone https://github.com/Nishant1195/BankLedger.git
cd BankLedger
docker compose up -d
JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64 mvn clean package
java -jar target/BankLedger-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 4. ACID Properties
| Property | What It Means | Implementation | Where In Code |
|---|---|---|---|
| **Atomicity** | All ops succeed or none do | `transfer()` wraps debit + credit in one transaction — credit failure rolls back the debit | [BankService.java](file:///home/nishant/Projects/BankLedger/src/main/java/com/bankladger/service/BankService.java) → `transfer()` |
| **Consistency** | DB stays valid before and after | CHECK constraint (`chk_balance_non_negative`) rejects negative balances at DB level; app pre-validates in `debit()` | [schema.sql](file:///home/nishant/Projects/BankLedger/schema.sql) + [AccountRepository.java](file:///home/nishant/Projects/BankLedger/src/main/java/com/bankladger/repository/AccountRepository.java) → `debit()` |
| **Isolation** | Concurrent transactions don't interfere | SERIALIZABLE isolation level set on connection creation — strictest level, prevents dirty reads, phantom reads, lost updates | [DBConnection.java](file:///home/nishant/Projects/BankLedger/src/main/java/com/bankladger/db/DBConnection.java) → `get()` |
| **Durability** | Committed data survives crashes | PostgreSQL WAL guarantees committed data persists across restarts — verified manually by killing and restarting the Docker container | [docker-compose.yml](file:///home/nishant/Projects/BankLedger/docker-compose.yml) + pgdata volume |

## 5. Durability Verification Steps
```bash
# 1. Run a transfer and confirm it commits
java -jar target/BankLedger-1.0-SNAPSHOT-jar-with-dependencies.jar

# 2. Kill the container (simulate crash)
docker compose stop

# 3. Restart the container
docker compose up -d

# 4. Query accounts directly — committed balances must be intact
docker exec -it bankledger-db psql -U postgres -d bankladger \
  -c "SELECT * FROM accounts;"
```

## 6. Project Structure
```text
BankLedger/
├── src/main/java/com/bankladger/
│   ├── Main.java               # CLI menu loop, input parsing only
│   ├── db/DBConnection.java    # Singleton JDBC connection, SERIALIZABLE isolation
│   ├── models/Account.java     # Account POJO
│   ├── models/TransactionRecord.java  # In-memory log record POJO
│   ├── repository/AccountRepository.java  # Raw SQL, no tx control
│   ├── service/BankService.java  # Transaction boundaries, commit/rollback
│   └── util/TransactionLogger.java  # In-memory HashMap + ArrayList session log
├── docker-compose.yml          # PostgreSQL 15 container config
├── schema.sql                  # Table definitions + CHECK constraint
└── pom.xml                     # Java 17, postgresql driver, assembly plugin
```

## 7. Known Limitations
- Single JDBC connection — no connection pool. Not suitable for concurrent use.
- CLI only — no REST API layer.
- No unit tests — ACID properties verified through manual end-to-end testing.
- SERIALIZABLE isolation increases transaction abort rate under high concurrency — acceptable for a demo, needs tuning for production.
- Credentials hardcoded in DBConnection.java — use environment variables in any real deployment.

## 8. Interview Quick Reference

**Q: What is ACID and where is each property in your code?**  
A: ACID represents the core pillars of relational database transactions. **Atomicity** ensures all operations within a transaction succeed or fail together, implemented in `BankService.java` → `transfer()` where any error during transfer rolls back the entire set of SQL operations. **Consistency** keeps the database state valid, implemented via PostgreSQL check constraints in `schema.sql` and manual fund checks in `AccountRepository.java` → `debit()`. **Isolation** prevents concurrent transactions from causing anomalies, implemented in `DBConnection.java` → `get()` by setting the isolation level to `TRANSACTION_SERIALIZABLE`. **Durability** guarantees that committed data persists, which is handled by PostgreSQL write-ahead logs and the Docker pgdata volume specified in `docker-compose.yml`.

**Q: Why JDBC and not Hibernate or Spring Data JPA?**  
A: Using raw JDBC is a conscious design decision to expose the mechanics of database connection lifecycles, statement execution, and transaction boundaries. Frameworks like Hibernate and Spring Data JPA hide these operations behind abstractions (like proxies, persistence contexts, and auto-flushing/dirty checking). By using prepared statements and manually calling `commit()` or `rollback()`, the codebase demonstrates exact control over database roundtrips, isolation properties, and transactional boundaries.

**Q: What happens if two users transfer from the same account simultaneously?**  
A: Because the database connection uses `TRANSACTION_SERIALIZABLE` isolation, PostgreSQL serializes access to rows. Under concurrent access, if two transactions try to modify the same account balance, one will be allowed to commit while the other is rejected with a serialization failure (`SQLState 40001`). This exception is caught in the `catch` block of the service, triggering a `rollback()` on the failed transaction and leaving the account balances correct and isolated.

**Q: What Collections did you use and why?**  
A: `TransactionLogger.java` utilizes `ArrayList` for the transaction history and `HashMap` for the account balance cache. The `ArrayList` is chosen because transaction log entries are appended sequentially in an ordered history list, which operates in O(1) time. The `HashMap` is chosen to cache retrieved account balances, enabling O(1) key-based lookups and updates mapping account IDs to balance values.

**Q: What's the difference between SERIALIZABLE and READ_COMMITTED?**  
A: `READ_COMMITTED` is the default isolation level in PostgreSQL and only prevents reading uncommitted data. However, it allows non-repeatable reads (data read twice within the same transaction can change) and phantom reads (rows can be inserted or deleted by another transaction). `SERIALIZABLE` is the strictest level, preventing all concurrent anomalies (including write skew and phantom reads) by locking or validating read/write sets to ensure transactions behave as if executed sequentially, though at the cost of aborting conflicting transactions.

**Q: What would you change if this needed to go to production?**  
A: Moving this application to production would require replacing the single, shared connection with a robust connection pool (such as HikariCP) to handle concurrent execution safely. Database credentials should be moved out of source code into environment variables or a secret manager. Additionally, the console-based menu loop would be replaced with a REST API layer (using a lightweight framework like Javalin or Spring Boot) and a thread-safe transaction logger. Finally, we would add automated testing with JUnit and Testcontainers to replace manual verification.
