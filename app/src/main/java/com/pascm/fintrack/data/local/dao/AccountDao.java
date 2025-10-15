package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.Account;

import java.util.List;

/**
 * Data Access Object for Accounts.
 */
@Dao
public interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Account account);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Account> accounts);

    @Update
    int update(Account account);

    @Delete
    int delete(Account account);

    @Query("DELETE FROM accounts WHERE account_id = :accountId")
    int deleteById(long accountId);

    // ========== Queries ==========

    @Query("SELECT * FROM accounts WHERE account_id = :accountId")
    LiveData<Account> getById(long accountId);

    @Query("SELECT * FROM accounts WHERE account_id = :accountId")
    Account getByIdSync(long accountId);

    @Query("SELECT * FROM accounts WHERE user_id = :userId AND archived = 0 ORDER BY created_at DESC")
    LiveData<List<Account>> getAllByUser(long userId);

    @Query("SELECT * FROM accounts WHERE user_id = :userId AND archived = 0 ORDER BY created_at DESC")
    List<Account> getAllByUserSync(long userId);

    @Query("SELECT * FROM accounts WHERE user_id = :userId AND type = :type AND archived = 0")
    LiveData<List<Account>> getByType(long userId, String type);

    @Query("UPDATE accounts SET balance = :newBalance, updated_at = :updatedAt WHERE account_id = :accountId")
    int updateBalance(long accountId, double newBalance, long updatedAt);

    @Query("UPDATE accounts SET archived = 1, updated_at = :updatedAt WHERE account_id = :accountId")
    int archive(long accountId, long updatedAt);

    @Query("SELECT SUM(balance) FROM accounts WHERE user_id = :userId AND archived = 0")
    LiveData<Double> getTotalBalance(long userId);

    @Query("SELECT COUNT(*) FROM accounts WHERE user_id = :userId AND archived = 0")
    LiveData<Integer> getAccountCount(long userId);
}
