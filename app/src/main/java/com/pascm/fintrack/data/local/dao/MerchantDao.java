package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.Merchant;

import java.util.List;

/**
 * Data Access Object for Merchants (Places/Establishments).
 *
 * Merchants represent physical or online places where transactions occur
 * (e.g., Walmart, Amazon, Starbucks). This replaces PlacesManager.
 */
@Dao
public interface MerchantDao {

    // ========== Insert Operations ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Merchant merchant);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Merchant> merchants);

    // ========== Update Operations ==========

    @Update
    int update(Merchant merchant);

    // ========== Delete Operations ==========

    @Delete
    int delete(Merchant merchant);

    @Query("DELETE FROM merchants WHERE merchant_id = :merchantId")
    int deleteById(long merchantId);

    // ========== Query Operations ==========

    /**
     * Get merchant by ID
     */
    @Query("SELECT * FROM merchants WHERE merchant_id = :merchantId")
    LiveData<Merchant> getById(long merchantId);

    @Query("SELECT * FROM merchants WHERE merchant_id = :merchantId")
    Merchant getByIdSync(long merchantId);

    /**
     * Get all merchants, ordered by name
     */
    @Query("SELECT * FROM merchants ORDER BY name ASC")
    LiveData<List<Merchant>> getAll();

    @Query("SELECT * FROM merchants ORDER BY name ASC")
    List<Merchant> getAllSync();

    /**
     * Get frequent merchants (used 5+ times)
     */
    @Query("SELECT * FROM merchants WHERE is_frequent = 1 ORDER BY name ASC")
    LiveData<List<Merchant>> getFrequentMerchants();

    /**
     * Search merchants by name or address
     */
    @Query("SELECT * FROM merchants WHERE " +
           "(name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%') " +
           "ORDER BY name ASC")
    LiveData<List<Merchant>> search(String query);

    /**
     * Get merchant by exact name
     */
    @Query("SELECT * FROM merchants WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    Merchant getByName(String name);

    /**
     * Get merchants with a specific tag
     */
    @Query("SELECT * FROM merchants WHERE tags LIKE '%' || :tag || '%' ORDER BY name ASC")
    LiveData<List<Merchant>> getByTag(String tag);

    /**
     * Get merchants near a location (simple bounding box)
     *
     * @param minLat Minimum latitude
     * @param maxLat Maximum latitude
     * @param minLng Minimum longitude
     * @param maxLng Maximum longitude
     */
    @Query("SELECT * FROM merchants WHERE " +
           "latitude BETWEEN :minLat AND :maxLat AND " +
           "longitude BETWEEN :minLng AND :maxLng " +
           "ORDER BY name ASC")
    LiveData<List<Merchant>> getNearby(double minLat, double maxLat, double minLng, double maxLng);

    /**
     * Get merchants marked as frequent (is_frequent = true)
     */
    @Query("SELECT * FROM merchants WHERE is_frequent = 1 ORDER BY usage_count DESC")
    LiveData<List<Merchant>> getFavorites();

    /**
     * Get most frequently used merchants (by usage count)
     */
    @Query("SELECT * FROM merchants " +
           "ORDER BY usage_count DESC " +
           "LIMIT :limit")
    LiveData<List<Merchant>> getMostUsed(int limit);

    /**
     * Get recently used merchants (merchants used in last N days)
     */
    @Query("SELECT DISTINCT m.* " +
           "FROM merchants m " +
           "INNER JOIN transactions t ON m.merchant_id = t.merchant_id " +
           "WHERE t.transaction_date >= :sinceDate " +
           "ORDER BY m.last_used_at DESC " +
           "LIMIT :limit")
    LiveData<List<Merchant>> getRecentlyUsed(long sinceDate, int limit);

    /**
     * Get merchant count
     */
    @Query("SELECT COUNT(*) FROM merchants")
    LiveData<Integer> getMerchantCount();

    /**
     * Get frequent merchant count
     */
    @Query("SELECT COUNT(*) FROM merchants WHERE is_frequent = 1")
    LiveData<Integer> getFrequentMerchantCount();

    /**
     * Check if merchant is being used in transactions
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE merchant_id = :merchantId")
    int getUsageCount(long merchantId);

    /**
     * Toggle frequent status
     */
    @Query("UPDATE merchants SET is_frequent = :isFrequent WHERE merchant_id = :merchantId")
    int updateFrequent(long merchantId, boolean isFrequent);

    /**
     * Update merchant location
     */
    @Query("UPDATE merchants SET latitude = :latitude, longitude = :longitude WHERE merchant_id = :merchantId")
    int updateLocation(long merchantId, double latitude, double longitude);

    /**
     * Update merchant tags
     */
    @Query("UPDATE merchants SET tags = :tags WHERE merchant_id = :merchantId")
    int updateTags(long merchantId, String tags);

    /**
     * Increment usage count
     */
    @Query("UPDATE merchants SET usage_count = usage_count + 1, last_used_at = :lastUsedAt WHERE merchant_id = :merchantId")
    int incrementUsageCount(long merchantId, long lastUsedAt);

    /**
     * Update usage count and last used time
     */
    @Query("UPDATE merchants SET usage_count = :usageCount, last_used_at = :lastUsedAt, is_frequent = CASE WHEN :usageCount >= 5 THEN 1 ELSE is_frequent END WHERE merchant_id = :merchantId")
    int updateUsageCount(long merchantId, int usageCount, long lastUsedAt);
}
