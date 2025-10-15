package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.CreditCardEntity;

import java.util.List;

/**
 * Data Access Object for Credit Cards.
 *
 * Provides methods to read/write credit card data from/to Room database.
 */
@Dao
public interface CreditCardDao {

    // ========== Insert Operations ==========

    /**
     * Insert a new credit card. Returns the auto-generated card_id.
     * If a card with the same ID exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CreditCardEntity card);

    /**
     * Insert multiple credit cards. Returns list of generated IDs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<CreditCardEntity> cards);

    // ========== Update Operations ==========

    /**
     * Update an existing credit card. Returns number of rows updated.
     */
    @Update
    int update(CreditCardEntity card);

    /**
     * Update only the balance of a card.
     */
    @Query("UPDATE credit_cards SET current_balance = :newBalance, updated_at = :updatedAt WHERE card_id = :cardId")
    int updateBalance(long cardId, double newBalance, long updatedAt);

    /**
     * Archive a card (soft delete).
     */
    @Query("UPDATE credit_cards SET archived = 1, updated_at = :updatedAt WHERE card_id = :cardId")
    int archive(long cardId, long updatedAt);

    // ========== Delete Operations ==========

    /**
     * Delete a credit card entity.
     */
    @Delete
    int delete(CreditCardEntity card);

    /**
     * Delete a credit card by ID.
     */
    @Query("DELETE FROM credit_cards WHERE card_id = :cardId")
    int deleteById(long cardId);

    // ========== Query Operations (LiveData - Reactive) ==========

    /**
     * Get a single credit card by ID (LiveData - observes changes).
     */
    @Query("SELECT * FROM credit_cards WHERE card_id = :cardId")
    LiveData<CreditCardEntity> getById(long cardId);

    /**
     * Get all credit cards for a user (non-archived, LiveData).
     */
    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND archived = 0 ORDER BY created_at DESC")
    LiveData<List<CreditCardEntity>> getAllByUser(long userId);

    /**
     * Get credit cards for a specific account (LiveData).
     */
    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND account_id = :accountId AND archived = 0 ORDER BY created_at DESC")
    LiveData<List<CreditCardEntity>> getByAccount(long userId, long accountId);

    /**
     * Get count of credit cards for a user (LiveData).
     */
    @Query("SELECT COUNT(*) FROM credit_cards WHERE user_id = :userId AND archived = 0")
    LiveData<Integer> getCardCount(long userId);

    // ========== Query Operations (Synchronous) ==========

    /**
     * Get a single credit card by ID (synchronous).
     * WARNING: Don't call on main thread!
     */
    @Query("SELECT * FROM credit_cards WHERE card_id = :cardId")
    CreditCardEntity getByIdSync(long cardId);

    /**
     * Get all credit cards for a user (synchronous).
     * WARNING: Don't call on main thread!
     */
    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND archived = 0 ORDER BY created_at DESC")
    List<CreditCardEntity> getAllByUserSync(long userId);

    /**
     * Get all archived credit cards for a user (synchronous).
     */
    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND archived = 1 ORDER BY updated_at DESC")
    List<CreditCardEntity> getArchivedByUserSync(long userId);

    /**
     * Get credit cards by brand (synchronous).
     */
    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND brand = :brand AND archived = 0")
    List<CreditCardEntity> getByBrandSync(long userId, String brand);

    // ========== Aggregate Queries ==========

    /**
     * Get total credit limit across all cards for a user.
     */
    @Query("SELECT SUM(credit_limit) FROM credit_cards WHERE user_id = :userId AND archived = 0")
    LiveData<Double> getTotalCreditLimit(long userId);

    /**
     * Get total balance across all cards for a user.
     */
    @Query("SELECT SUM(current_balance) FROM credit_cards WHERE user_id = :userId AND archived = 0")
    LiveData<Double> getTotalBalance(long userId);

    /**
     * Get total available credit across all cards.
     */
    @Query("SELECT SUM(credit_limit - current_balance) FROM credit_cards WHERE user_id = :userId AND archived = 0")
    LiveData<Double> getTotalAvailableCredit(long userId);
}
