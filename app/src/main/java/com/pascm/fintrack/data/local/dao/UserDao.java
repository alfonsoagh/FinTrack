package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.User;
import com.pascm.fintrack.data.local.entity.UserProfile;

import java.util.List;

/**
 * Data Access Object for Users.
 *
 * Handles user authentication and profile management queries.
 */
@Dao
public interface UserDao {

    // ========== Insert Operations ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<User> users);

    // Perfil de usuario
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProfile(UserProfile profile);

    // ========== Update Operations ==========

    @Update
    int update(User user);

    @Update
    int updateProfile(UserProfile profile);

    /**
     * Update last login timestamp
     */
    @Query("UPDATE users SET last_login_at = :lastLoginAt, updated_at = :updatedAt WHERE user_id = :userId")
    int updateLastLogin(long userId, long lastLoginAt, long updatedAt);

    /**
     * Update user status
     */
    @Query("UPDATE users SET status = :status, updated_at = :updatedAt WHERE user_id = :userId")
    int updateStatus(long userId, String status, long updatedAt);

    /**
     * Update password hash
     */
    @Query("UPDATE users SET password_hash = :newHash, updated_at = :updatedAt WHERE user_id = :userId")
    int updatePasswordHash(long userId, String newHash, long updatedAt);

    // ========== Delete Operations ==========

    @Delete
    int delete(User user);

    @Query("DELETE FROM users WHERE user_id = :userId")
    int deleteById(long userId);

    // ========== Query Operations ==========

    /**
     * Get user by ID (LiveData)
     */
    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<User> getById(long userId);

    /**
     * Get user by ID (synchronous)
     */
    @Query("SELECT * FROM users WHERE user_id = :userId")
    User getByIdSync(long userId);

    /**
     * Get user by email (synchronous)
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getByEmail(String email);

    /**
     * Get user by email (LiveData)
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    LiveData<User> getByEmailLive(String email);

    /**
     * Get user by Firebase UID
     */
    @Query("SELECT * FROM users WHERE firebase_uid = :firebaseUid LIMIT 1")
    User getByFirebaseUid(String firebaseUid);

    /**
     * Get all users (admin function)
     */
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    LiveData<List<User>> getAllUsers();

    /**
     * Get users by status
     */
    @Query("SELECT * FROM users WHERE status = :status ORDER BY created_at DESC")
    LiveData<List<User>> getUsersByStatus(String status);

    /**
     * Check if email exists
     */
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int emailExists(String email);

    /**
     * Get user count
     */
    @Query("SELECT COUNT(*) FROM users")
    LiveData<Integer> getUserCount();

    // ========== UserProfile Queries ==========

    @Query("SELECT * FROM user_profiles WHERE user_id = :userId LIMIT 1")
    LiveData<UserProfile> getProfile(long userId);
}
