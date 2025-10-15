package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.Transaction;

import java.time.Instant;
import java.util.List;

/**
 * Data Access Object for Transactions.
 *
 * Provides comprehensive queries for transaction management including:
 * - CRUD operations
 * - Filtering by date, category, type, account, trip
 * - Aggregate calculations (sum, count, average)
 * - Expense/Income analysis
 */
@Dao
public interface TransactionDao {

    // ========== Insert Operations ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Transaction transaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Transaction> transactions);

    // ========== Update Operations ==========

    @Update
    int update(Transaction transaction);

    @Query("UPDATE transactions SET status = :status, updated_at = :updatedAt WHERE transaction_id = :transactionId")
    int updateStatus(long transactionId, String status, long updatedAt);

    // ========== Delete Operations ==========

    @Delete
    int delete(Transaction transaction);

    @Query("DELETE FROM transactions WHERE transaction_id = :transactionId")
    int deleteById(long transactionId);

    // ========== Basic Queries ==========

    @Query("SELECT * FROM transactions WHERE transaction_id = :transactionId")
    LiveData<Transaction> getById(long transactionId);

    @Query("SELECT * FROM transactions WHERE transaction_id = :transactionId")
    Transaction getByIdSync(long transactionId);

    /**
     * Get all transactions for a user, ordered by date (newest first)
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getAllByUser(long userId);

    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date DESC LIMIT :limit")
    LiveData<List<Transaction>> getRecentByUser(long userId, int limit);

    // ========== Filtered Queries ==========

    /**
     * Get transactions by type (INCOME, EXPENSE, TRANSFER)
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND type = :type ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getByType(long userId, String type);

    /**
     * Get transactions by category
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND category_id = :categoryId ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getByCategory(long userId, long categoryId);

    /**
     * Get transactions by account
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND account_id = :accountId ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getByAccount(long userId, long accountId);

    /**
     * Get transactions by card
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND card_id = :cardId ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getByCard(long userId, long cardId);

    /**
     * Get transactions for a trip
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND trip_id = :tripId ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getByTrip(long userId, long tripId);

    /**
     * Get transactions by merchant
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND merchant_id = :merchantId ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getByMerchant(long userId, long merchantId);

    // ========== Date Range Queries ==========

    /**
     * Get transactions within date range
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND transaction_date BETWEEN :startDate AND :endDate ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getByDateRange(long userId, long startDate, long endDate);

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND transaction_date BETWEEN :startDate AND :endDate ORDER BY transaction_date DESC")
    List<Transaction> getByDateRangeSync(long userId, long startDate, long endDate);

    /**
     * Get transactions for current month
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND strftime('%Y-%m', datetime(transaction_date/1000, 'unixepoch')) = strftime('%Y-%m', 'now') ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getCurrentMonth(long userId);

    // ========== Aggregate Queries ==========

    /**
     * Get total expense amount for a user
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'EXPENSE' AND status = 'COMPLETED'")
    LiveData<Double> getTotalExpenses(long userId);

    /**
     * Get total income amount for a user
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'INCOME' AND status = 'COMPLETED'")
    LiveData<Double> getTotalIncome(long userId);

    /**
     * Get balance (income - expenses)
     */
    @Query("SELECT " +
            "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) - " +
            "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) " +
            "FROM transactions WHERE user_id = :userId AND status = 'COMPLETED'")
    LiveData<Double> getBalance(long userId);

    /**
     * Get total expenses by category
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND category_id = :categoryId AND type = 'EXPENSE' AND status = 'COMPLETED'")
    LiveData<Double> getTotalExpensesByCategory(long userId, long categoryId);

    /**
     * Get expenses for date range
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'EXPENSE' AND status = 'COMPLETED' AND transaction_date BETWEEN :startDate AND :endDate")
    LiveData<Double> getExpensesInRange(long userId, long startDate, long endDate);

    /**
     * Get income for date range
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'INCOME' AND status = 'COMPLETED' AND transaction_date BETWEEN :startDate AND :endDate")
    LiveData<Double> getIncomeInRange(long userId, long startDate, long endDate);

    /**
     * Get transaction count
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE user_id = :userId")
    LiveData<Integer> getTransactionCount(long userId);

    /**
     * Get average transaction amount
     */
    @Query("SELECT AVG(amount) FROM transactions WHERE user_id = :userId AND type = :type AND status = 'COMPLETED'")
    LiveData<Double> getAverageAmount(long userId, String type);

    // ========== Search Queries ==========

    /**
     * Search transactions by notes (full-text search)
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND notes LIKE '%' || :searchQuery || '%' ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> search(long userId, String searchQuery);

    /**
     * Get transactions with attachments
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND attachments IS NOT NULL AND attachments != '[]' ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getWithAttachments(long userId);

    /**
     * Get transactions with location
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND latitude IS NOT NULL AND longitude IS NOT NULL ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getWithLocation(long userId);

    // ========== Sync Queries ==========

    /**
     * Get transactions that need syncing (dirty)
     */
    @Query("SELECT * FROM transactions WHERE synced_at IS NULL OR updated_at > synced_at")
    List<Transaction> getUnsynced();

    /**
     * Mark transaction as synced
     */
    @Query("UPDATE transactions SET synced_at = :syncedAt WHERE transaction_id = :transactionId")
    int markSynced(long transactionId, long syncedAt);

    // ========== Statistics Queries ==========

    /**
     * Get spending by category (for charts/reports)
     * Returns category_id and total amount
     */
    @Query("SELECT category_id, SUM(amount) as total FROM transactions " +
            "WHERE user_id = :userId AND type = 'EXPENSE' AND status = 'COMPLETED' " +
            "GROUP BY category_id ORDER BY total DESC")
    List<CategoryExpense> getSpendingByCategory(long userId);

    /**
     * POJO for category expense results
     */
    class CategoryExpense {
        public Long category_id;
        public Double total;
    }

    /**
     * Get daily expenses for last N days
     */
    @Query("SELECT strftime('%Y-%m-%d', datetime(transaction_date/1000, 'unixepoch')) as date, SUM(amount) as total " +
            "FROM transactions " +
            "WHERE user_id = :userId AND type = 'EXPENSE' AND status = 'COMPLETED' " +
            "AND transaction_date >= :sinceDate " +
            "GROUP BY date ORDER BY date DESC")
    List<DailyExpense> getDailyExpenses(long userId, long sinceDate);

    /**
     * POJO for daily expense results
     */
    class DailyExpense {
        public String date;
        public Double total;
    }

    /**
     * Get spending by month for a year (for trend analysis)
     */
    @Query("SELECT strftime('%m', datetime(transaction_date/1000, 'unixepoch')) as month, SUM(amount) as total " +
            "FROM transactions " +
            "WHERE user_id = :userId AND type = 'EXPENSE' AND status = 'COMPLETED' " +
            "AND strftime('%Y', datetime(transaction_date/1000, 'unixepoch')) = CAST(:year AS TEXT) " +
            "GROUP BY month ORDER BY month ASC")
    List<MonthlyExpense> getSpendingByMonth(long userId, int year);

    /**
     * POJO for monthly expense results
     */
    class MonthlyExpense {
        public String month;
        public Double total;
    }

    // ========== Additional Methods for Repository ==========

    /**
     * Get all transactions synchronously
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date DESC")
    List<Transaction> getAllByUserSync(long userId);

    /**
     * Get recent transactions
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date DESC LIMIT :limit")
    LiveData<List<Transaction>> getRecent(long userId, int limit);

    /**
     * Get pending transactions
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND status = 'PENDING' ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getPending(long userId);

    /**
     * Get balance synchronously
     */
    @Query("SELECT " +
            "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) - " +
            "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) " +
            "FROM transactions WHERE user_id = :userId AND status = 'COMPLETED'")
    Double getBalanceSync(long userId);

    /**
     * Get expenses for date range
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'EXPENSE' AND status = 'COMPLETED' AND transaction_date BETWEEN :startDate AND :endDate")
    LiveData<Double> getExpensesForDateRange(long userId, long startDate, long endDate);

    /**
     * Get income for date range
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'INCOME' AND status = 'COMPLETED' AND transaction_date BETWEEN :startDate AND :endDate")
    LiveData<Double> getIncomeForDateRange(long userId, long startDate, long endDate);

    /**
     * Get average transaction amount
     */
    @Query("SELECT AVG(amount) FROM transactions WHERE user_id = :userId AND status = 'COMPLETED'")
    LiveData<Double> getAverageAmount(long userId);

    /**
     * Get transaction count for date range
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE user_id = :userId AND transaction_date BETWEEN :startDate AND :endDate")
    LiveData<Integer> getTransactionCountForDateRange(long userId, long startDate, long endDate);

    /**
     * Update trip ID for a transaction
     */
    @Query("UPDATE transactions SET trip_id = :tripId, updated_at = :updatedAt WHERE transaction_id = :transactionId")
    int updateTripId(long transactionId, long tripId, long updatedAt);

    /**
     * Delete multiple transactions
     */
    @Delete
    void deleteAll(List<Transaction> transactions);

    /**
     * Get transactions by card (overload with cardType)
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND card_id = :cardId AND card_type = :cardType ORDER BY transaction_date DESC")
    LiveData<List<Transaction>> getByCard(long userId, long cardId, String cardType);
}
