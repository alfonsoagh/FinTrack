package com.pascm.fintrack.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.TransactionDao;
import com.pascm.fintrack.data.local.entity.Transaction;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Repository for managing transactions.
 *
 * This repository provides a clean API for all transaction operations including:
 * - CRUD operations
 * - Date range queries
 * - Statistics and aggregations
 * - Search and filtering
 * - Trip association
 *
 * Usage Example:
 * <pre>
 * TransactionRepository repository = new TransactionRepository(context);
 *
 * // Create transaction
 * Transaction transaction = new Transaction();
 * transaction.setUserId(userId);
 * transaction.setAmount(150.00);
 * transaction.setType(Transaction.TransactionType.EXPENSE);
 * repository.insertTransaction(transaction);
 *
 * // Get transactions for current month
 * LiveData<List<Transaction>> monthTransactions = repository.getTransactionsForMonth(userId, 2025, 1);
 * monthTransactions.observe(owner, transactions -> {
 *     // Update UI
 * });
 *
 * // Get spending by category
 * repository.getSpendingByCategory(userId).observe(owner, spending -> {
 *     // Display chart
 * });
 * </pre>
 */
public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final FinTrackDatabase database;

    public TransactionRepository(Context context) {
        this.database = FinTrackDatabase.getDatabase(context);
        this.transactionDao = database.transactionDao();
    }

    // ========== Read Operations (Reactive with LiveData) ==========

    /**
     * Get all transactions for a user, ordered by date (newest first)
     */
    public LiveData<List<Transaction>> getAllTransactions(long userId) {
        return transactionDao.getAllByUser(userId);
    }

    /**
     * Get a specific transaction by ID
     */
    public LiveData<Transaction> getTransactionById(long transactionId) {
        return transactionDao.getById(transactionId);
    }

    /**
     * Get transactions within a date range
     *
     * @param userId    User ID
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     */
    public LiveData<List<Transaction>> getTransactionsByDateRange(long userId, LocalDate startDate, LocalDate endDate) {
        long startEpochMilli = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpochMilli = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return transactionDao.getByDateRange(userId, startEpochMilli, endEpochMilli);
    }

    /**
     * Get transactions for a specific month
     *
     * @param userId User ID
     * @param year   Year (e.g., 2025)
     * @param month  Month (1-12)
     */
    public LiveData<List<Transaction>> getTransactionsForMonth(long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return getTransactionsByDateRange(userId, startDate, endDate);
    }

    /**
     * Get transactions for current month
     */
    public LiveData<List<Transaction>> getTransactionsForCurrentMonth(long userId) {
        LocalDate now = LocalDate.now();
        return getTransactionsForMonth(userId, now.getYear(), now.getMonthValue());
    }

    /**
     * Get transactions by type (INCOME or EXPENSE)
     */
    public LiveData<List<Transaction>> getTransactionsByType(long userId, Transaction.TransactionType type) {
        return transactionDao.getByType(userId, type.name());
    }

    /**
     * Get transactions by category
     */
    public LiveData<List<Transaction>> getTransactionsByCategory(long userId, long categoryId) {
        return transactionDao.getByCategory(userId, categoryId);
    }

    /**
     * Get transactions by account
     */
    public LiveData<List<Transaction>> getTransactionsByAccount(long userId, long accountId) {
        return transactionDao.getByAccount(userId, accountId);
    }

    /**
     * Get transactions by card
     */
    public LiveData<List<Transaction>> getTransactionsByCard(long userId, long cardId, String cardType) {
        return transactionDao.getByCard(userId, cardId, cardType);
    }

    /**
     * Get transactions associated with a trip
     */
    public LiveData<List<Transaction>> getTransactionsByTrip(long userId, long tripId) {
        return transactionDao.getByTrip(userId, tripId);
    }

    /**
     * Search transactions by notes or merchant name
     */
    public LiveData<List<Transaction>> searchTransactions(long userId, String query) {
        return transactionDao.search(userId, query);
    }

    /**
     * Get pending transactions (status = PENDING)
     */
    public LiveData<List<Transaction>> getPendingTransactions(long userId) {
        return transactionDao.getPending(userId);
    }

    /**
     * Get recent transactions (last N)
     */
    public LiveData<List<Transaction>> getRecentTransactions(long userId, int limit) {
        return transactionDao.getRecent(userId, limit);
    }

    // ========== Statistics and Aggregations ==========

    /**
     * Get total income for user
     */
    public LiveData<Double> getTotalIncome(long userId) {
        return transactionDao.getTotalIncome(userId);
    }

    /**
     * Get total expenses for user
     */
    public LiveData<Double> getTotalExpenses(long userId) {
        return transactionDao.getTotalExpenses(userId);
    }

    /**
     * Get current balance (income - expenses)
     */
    public LiveData<Double> getBalance(long userId) {
        return transactionDao.getBalance(userId);
    }

    /**
     * Get total income for date range
     */
    public LiveData<Double> getIncomeForDateRange(long userId, LocalDate startDate, LocalDate endDate) {
        long startEpochMilli = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpochMilli = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return transactionDao.getIncomeForDateRange(userId, startEpochMilli, endEpochMilli);
    }

    /**
     * Get total expenses for date range
     */
    public LiveData<Double> getExpensesForDateRange(long userId, LocalDate startDate, LocalDate endDate) {
        long startEpochMilli = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpochMilli = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return transactionDao.getExpensesForDateRange(userId, startEpochMilli, endEpochMilli);
    }

    /**
     * Get income for current month
     */
    public LiveData<Double> getIncomeForCurrentMonth(long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return getIncomeForDateRange(userId, startDate, endDate);
    }

    /**
     * Get expenses for current month
     */
    public LiveData<Double> getExpensesForCurrentMonth(long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return getExpensesForDateRange(userId, startDate, endDate);
    }

    /**
     * Get average transaction amount
     */
    public LiveData<Double> getAverageTransactionAmount(long userId) {
        return transactionDao.getAverageAmount(userId);
    }

    /**
     * Get spending by category (for charts/analytics)
     *
     * Returns a list of CategoryExpense objects with category_id and total amount
     */
    public List<TransactionDao.CategoryExpense> getSpendingByCategorySync(long userId) {
        return transactionDao.getSpendingByCategory(userId);
    }

    /**
     * Get spending by month (for trend analysis)
     */
    public List<TransactionDao.MonthlyExpense> getSpendingByMonthSync(long userId, int year) {
        return transactionDao.getSpendingByMonth(userId, year);
    }

    /**
     * Get transaction count
     */
    public LiveData<Integer> getTransactionCount(long userId) {
        return transactionDao.getTransactionCount(userId);
    }

    /**
     * Get transaction count for date range
     */
    public LiveData<Integer> getTransactionCountForDateRange(long userId, LocalDate startDate, LocalDate endDate) {
        long startEpochMilli = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpochMilli = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return transactionDao.getTransactionCountForDateRange(userId, startEpochMilli, endEpochMilli);
    }

    // ========== Write Operations (Async) ==========

    /**
     * Insert a new transaction
     *
     * Automatically sets createdAt and updatedAt timestamps.
     * Marks transaction for sync with Firebase.
     *
     * @param transaction Transaction to insert
     */
    public void insertTransaction(Transaction transaction) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            Instant now = Instant.now();
            transaction.setCreatedAt(now);
            transaction.setUpdatedAt(now);

            // Set default status if not set
            if (transaction.getStatus() == null) {
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            }

            // Set default currency if not set
            if (transaction.getCurrencyCode() == null || transaction.getCurrencyCode().isEmpty()) {
                transaction.setCurrencyCode("MXN");
            }

            long transactionId = transactionDao.insert(transaction);

            android.util.Log.i("TransactionRepository", "Inserted transaction ID: " + transactionId);

            // TODO: Mark for sync
            // SyncRepository.markForSync("TRANSACTION", transactionId, "CREATE");
        });
    }

    /**
     * Insert multiple transactions at once (batch operation)
     */
    public void insertTransactions(List<Transaction> transactions) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            Instant now = Instant.now();
            for (Transaction transaction : transactions) {
                transaction.setCreatedAt(now);
                transaction.setUpdatedAt(now);

                if (transaction.getStatus() == null) {
                    transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                }
                if (transaction.getCurrencyCode() == null || transaction.getCurrencyCode().isEmpty()) {
                    transaction.setCurrencyCode("MXN");
                }
            }

            List<Long> ids = transactionDao.insertAll(transactions);
            android.util.Log.i("TransactionRepository", "Inserted " + ids.size() + " transactions");

            // TODO: Mark all for sync
        });
    }

    /**
     * Update an existing transaction
     */
    public void updateTransaction(Transaction transaction) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            transaction.setUpdatedAt(Instant.now());
            int rowsUpdated = transactionDao.update(transaction);

            if (rowsUpdated > 0) {
                android.util.Log.i("TransactionRepository", "Updated transaction ID: " + transaction.getTransactionId());

                // TODO: Mark for sync
                // SyncRepository.markForSync("TRANSACTION", transaction.getTransactionId(), "UPDATE");
            }
        });
    }

    /**
     * Update transaction status
     */
    public void updateTransactionStatus(long transactionId, Transaction.TransactionStatus status) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.updateStatus(transactionId, status.name(), Instant.now().toEpochMilli());

            // TODO: Mark for sync
        });
    }

    /**
     * Delete a transaction (soft delete by setting status to CANCELLED)
     *
     * Recommended approach: Don't physically delete, just cancel
     */
    public void cancelTransaction(long transactionId) {
        updateTransactionStatus(transactionId, Transaction.TransactionStatus.CANCELLED);
    }

    /**
     * Delete a transaction permanently (use with caution!)
     */
    public void deleteTransaction(Transaction transaction) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            int rowsDeleted = transactionDao.delete(transaction);

            if (rowsDeleted > 0) {
                android.util.Log.i("TransactionRepository", "Deleted transaction ID: " + transaction.getTransactionId());

                // TODO: Mark for sync (DELETE operation)
                // SyncRepository.markForSync("TRANSACTION", transaction.getTransactionId(), "DELETE");
            }
        });
    }

    /**
     * Delete multiple transactions (batch operation)
     */
    public void deleteTransactions(List<Transaction> transactions) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.deleteAll(transactions);

            // TODO: Mark all for sync
        });
    }

    // ========== Synchronous Operations (Use with caution - don't call on main thread!) ==========

    /**
     * Insert a new transaction (synchronous)
     *
     * Automatically sets createdAt and updatedAt timestamps.
     * Returns the generated transaction ID.
     *
     * WARNING: Don't call on main thread!
     *
     * @param transaction Transaction to insert
     * @return The generated transaction ID
     */
    public long insertTransactionSync(Transaction transaction) {
        Instant now = Instant.now();
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        // Set default status if not set
        if (transaction.getStatus() == null) {
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        }

        // Set default currency if not set
        if (transaction.getCurrencyCode() == null || transaction.getCurrencyCode().isEmpty()) {
            transaction.setCurrencyCode("MXN");
        }

        long transactionId = transactionDao.insert(transaction);

        android.util.Log.i("TransactionRepository", "Inserted transaction ID: " + transactionId);

        // TODO: Mark for sync
        // SyncRepository.markForSync("TRANSACTION", transactionId, "CREATE");

        return transactionId;
    }

    /**
     * Get transaction by ID (synchronous)
     *
     * WARNING: Don't call on main thread!
     */
    public Transaction getTransactionByIdSync(long transactionId) {
        return transactionDao.getByIdSync(transactionId);
    }

    /**
     * Get all transactions (synchronous)
     *
     * WARNING: Don't call on main thread!
     */
    public List<Transaction> getAllTransactionsSync(long userId) {
        return transactionDao.getAllByUserSync(userId);
    }

    /**
     * Get balance (synchronous)
     *
     * WARNING: Don't call on main thread!
     */
    public double getBalanceSync(long userId) {
        Double balance = transactionDao.getBalanceSync(userId);
        return balance != null ? balance : 0.0;
    }

    // ========== Helper Methods ==========

    /**
     * Create a quick expense transaction (for simple use cases)
     */
    public void createQuickExpense(long userId, double amount, String notes) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(Transaction.TransactionType.EXPENSE);
        transaction.setNotes(notes);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setTransactionDate(Instant.now());

        insertTransaction(transaction);
    }

    /**
     * Create a quick income transaction
     */
    public void createQuickIncome(long userId, double amount, String notes) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(Transaction.TransactionType.INCOME);
        transaction.setNotes(notes);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setTransactionDate(Instant.now());

        insertTransaction(transaction);
    }

    /**
     * Associate transaction with active trip (if any)
     *
     * This should be called when creating a transaction if you want to
     * automatically link it to the current active trip.
     */
    public void associateWithActiveTrip(long transactionId, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            // Get active trip
            var tripDao = database.tripDao();
            var activeTrip = tripDao.getActiveTripSync(userId);

            if (activeTrip != null) {
                transactionDao.updateTripId(transactionId, activeTrip.getTripId(), Instant.now().toEpochMilli());
                android.util.Log.i("TransactionRepository",
                    "Associated transaction " + transactionId + " with trip " + activeTrip.getTripId());
            }
        });
    }

    /**
     * Update card balance after transaction
     *
     * This should be called after inserting a transaction linked to a credit card
     * to update the card's current balance.
     *
     * @param cardId      Card ID
     * @param amount      Transaction amount (positive for charges, negative for payments)
     * @param isCredit    true for credit card, false for debit card
     */
    public void updateCardBalanceAfterTransaction(long cardId, double amount, boolean isCredit) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            if (isCredit) {
                var creditCardDao = database.creditCardDao();
                var card = creditCardDao.getByIdSync(cardId);

                if (card != null) {
                    double newBalance = card.getCurrentBalance() + amount;
                    creditCardDao.updateBalance(cardId, newBalance, Instant.now().toEpochMilli());

                    android.util.Log.i("TransactionRepository",
                        "Updated card balance: " + card.getCurrentBalance() + " -> " + newBalance);
                }
            }
            // TODO: Handle debit cards when DebitCardDao is implemented
        });
    }

    /**
     * Update account balance after transaction
     *
     * This should be called after inserting a transaction linked to an account.
     *
     * @param accountId Account ID
     * @param amount    Transaction amount (positive for income, negative for expense)
     */
    public void updateAccountBalanceAfterTransaction(long accountId, double amount) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            var accountDao = database.accountDao();
            var account = accountDao.getByIdSync(accountId);

            if (account != null) {
                double newBalance = account.getBalance() + amount;
                accountDao.updateBalance(accountId, newBalance, Instant.now().toEpochMilli());

                android.util.Log.i("TransactionRepository",
                    "Updated account balance: " + account.getBalance() + " -> " + newBalance);
            }
        });
    }
}
