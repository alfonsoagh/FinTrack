package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.DebitCardEntity;

import java.util.List;

/**
 * Data Access Object for Debit Cards.
 *
 * Provides methods for managing debit cards linked to bank accounts.
 */
@Dao
public interface DebitCardDao {

    // ========== Insert Operations ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DebitCardEntity card);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<DebitCardEntity> cards);

    // ========== Update Operations ==========

    @Update
    int update(DebitCardEntity card);

    // ========== Delete Operations ==========

    @Delete
    int delete(DebitCardEntity card);

    @Query("DELETE FROM debit_cards WHERE card_id = :cardId")
    int deleteById(long cardId);

    // ========== Query Operations ==========

    /**
     * Get debit card by ID
     */
    @Query("SELECT * FROM debit_cards WHERE card_id = :cardId")
    LiveData<DebitCardEntity> getById(long cardId);

    @Query("SELECT * FROM debit_cards WHERE card_id = :cardId")
    DebitCardEntity getByIdSync(long cardId);

    /**
     * Get all debit cards for a user
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND archived = 0 ORDER BY is_primary DESC, created_at DESC")
    LiveData<List<DebitCardEntity>> getAllByUser(long userId);

    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND archived = 0 ORDER BY is_primary DESC, created_at DESC")
    List<DebitCardEntity> getAllByUserSync(long userId);

    /**
     * Get active debit cards (not archived and is_active = true)
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND archived = 0 AND is_active = 1 ORDER BY is_primary DESC, created_at DESC")
    LiveData<List<DebitCardEntity>> getActiveCards(long userId);

    /**
     * Get debit cards linked to a specific account
     */
    @Query("SELECT * FROM debit_cards WHERE account_id = :accountId AND archived = 0 ORDER BY is_primary DESC, created_at DESC")
    LiveData<List<DebitCardEntity>> getByAccount(long accountId);

    @Query("SELECT * FROM debit_cards WHERE account_id = :accountId AND archived = 0 ORDER BY is_primary DESC, created_at DESC")
    List<DebitCardEntity> getByAccountSync(long accountId);

    /**
     * Get primary debit card for user
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND is_primary = 1 AND archived = 0 LIMIT 1")
    LiveData<DebitCardEntity> getPrimaryCard(long userId);

    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND is_primary = 1 AND archived = 0 LIMIT 1")
    DebitCardEntity getPrimaryCardSync(long userId);

    /**
     * Get debit cards by issuer/bank
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND issuer = :issuer AND archived = 0 ORDER BY created_at DESC")
    LiveData<List<DebitCardEntity>> getByIssuer(long userId, String issuer);

    /**
     * Get debit cards by brand (VISA, MASTERCARD, etc.)
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND brand = :brand AND archived = 0 ORDER BY created_at DESC")
    LiveData<List<DebitCardEntity>> getByBrand(long userId, String brand);

    /**
     * Get physical debit cards only
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND card_type = 'PHYSICAL' AND archived = 0 ORDER BY is_primary DESC, created_at DESC")
    LiveData<List<DebitCardEntity>> getPhysicalCards(long userId);

    /**
     * Get virtual debit cards only
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND card_type = 'VIRTUAL' AND archived = 0 ORDER BY created_at DESC")
    LiveData<List<DebitCardEntity>> getVirtualCards(long userId);

    /**
     * Get expiring cards (expiry_date within next N days)
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND expiry_date IS NOT NULL AND expiry_date <= :expiryThreshold AND archived = 0 ORDER BY expiry_date ASC")
    LiveData<List<DebitCardEntity>> getExpiringCards(long userId, long expiryThreshold);

    /**
     * Get expired cards
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND expiry_date IS NOT NULL AND expiry_date < :currentDate AND archived = 0 ORDER BY expiry_date DESC")
    LiveData<List<DebitCardEntity>> getExpiredCards(long userId, long currentDate);

    /**
     * Search debit cards by label or issuer
     */
    @Query("SELECT * FROM debit_cards WHERE user_id = :userId AND (label LIKE '%' || :query || '%' OR issuer LIKE '%' || :query || '%') AND archived = 0 ORDER BY created_at DESC")
    LiveData<List<DebitCardEntity>> search(long userId, String query);

    /**
     * Get debit card count
     */
    @Query("SELECT COUNT(*) FROM debit_cards WHERE user_id = :userId AND archived = 0")
    LiveData<Integer> getCardCount(long userId);

    /**
     * Get active debit card count
     */
    @Query("SELECT COUNT(*) FROM debit_cards WHERE user_id = :userId AND archived = 0 AND is_active = 1")
    LiveData<Integer> getActiveCardCount(long userId);

    /**
     * Get card count by account
     */
    @Query("SELECT COUNT(*) FROM debit_cards WHERE account_id = :accountId AND archived = 0")
    LiveData<Integer> getCardCountByAccount(long accountId);

    // ========== Update Specific Fields ==========

    /**
     * Set primary debit card (and unset others)
     *
     * Note: Should be called in a transaction to ensure only one primary card
     */
    @Query("UPDATE debit_cards SET is_primary = CASE WHEN card_id = :cardId THEN 1 ELSE 0 END, updated_at = :updatedAt WHERE user_id = :userId")
    int setPrimaryCard(long userId, long cardId, long updatedAt);

    /**
     * Update card status (active/inactive)
     */
    @Query("UPDATE debit_cards SET is_active = :isActive, updated_at = :updatedAt WHERE card_id = :cardId")
    int updateStatus(long cardId, boolean isActive, long updatedAt);

    /**
     * Update daily limit
     */
    @Query("UPDATE debit_cards SET daily_limit = :dailyLimit, updated_at = :updatedAt WHERE card_id = :cardId")
    int updateDailyLimit(long cardId, double dailyLimit, long updatedAt);

    /**
     * Update expiry date
     */
    @Query("UPDATE debit_cards SET expiry_date = :expiryDate, updated_at = :updatedAt WHERE card_id = :cardId")
    int updateExpiryDate(long cardId, long expiryDate, long updatedAt);

    /**
     * Archive a debit card (soft delete)
     */
    @Query("UPDATE debit_cards SET archived = 1, updated_at = :updatedAt WHERE card_id = :cardId")
    int archive(long cardId, long updatedAt);

    /**
     * Unarchive a debit card
     */
    @Query("UPDATE debit_cards SET archived = 0, updated_at = :updatedAt WHERE card_id = :cardId")
    int unarchive(long cardId, long updatedAt);

    /**
     * Update Firebase ID (for sync)
     */
    @Query("UPDATE debit_cards SET firebase_id = :firebaseId, updated_at = :updatedAt WHERE card_id = :cardId")
    int updateFirebaseId(long cardId, String firebaseId, long updatedAt);

    // ========== Aggregations ==========

    /**
     * Get total daily limit across all active cards
     */
    @Query("SELECT SUM(daily_limit) FROM debit_cards WHERE user_id = :userId AND archived = 0 AND is_active = 1 AND daily_limit IS NOT NULL")
    LiveData<Double> getTotalDailyLimit(long userId);

    /**
     * Count cards by issuer
     */
    @Query("SELECT issuer, COUNT(*) as count FROM debit_cards WHERE user_id = :userId AND archived = 0 GROUP BY issuer ORDER BY count DESC")
    List<IssuerCount> getCardCountByIssuer(long userId);

    /**
     * Helper class for issuer count aggregation
     */
    class IssuerCount {
        public String issuer;
        public int count;
    }
}
