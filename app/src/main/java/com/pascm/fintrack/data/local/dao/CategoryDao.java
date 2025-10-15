package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.Category;

import java.util.List;

/**
 * Data Access Object for Categories.
 *
 * Categories organize transactions into groups (e.g., Food, Transport, Entertainment).
 * Each category can be for expenses or income, and has visual properties (icon, color).
 * Categories are shared across all users.
 */
@Dao
public interface CategoryDao {

    // ========== Insert Operations ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Category> categories);

    // ========== Update Operations ==========

    @Update
    int update(Category category);

    // ========== Delete Operations ==========

    @Delete
    int delete(Category category);

    @Query("DELETE FROM categories WHERE category_id = :categoryId")
    int deleteById(long categoryId);

    // ========== Query Operations ==========

    /**
     * Get category by ID
     */
    @Query("SELECT * FROM categories WHERE category_id = :categoryId")
    LiveData<Category> getById(long categoryId);

    @Query("SELECT * FROM categories WHERE category_id = :categoryId")
    Category getByIdSync(long categoryId);

    /**
     * Get all categories
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAllByUser();

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllByUserSync();

    /**
     * Get expense categories
     */
    @Query("SELECT * FROM categories WHERE is_expense = 1 ORDER BY name ASC")
    LiveData<List<Category>> getExpenseCategories();

    /**
     * Get income categories
     */
    @Query("SELECT * FROM categories WHERE is_expense = 0 ORDER BY name ASC")
    LiveData<List<Category>> getIncomeCategories();

    /**
     * Get active categories
     */
    @Query("SELECT * FROM categories WHERE active = 1 ORDER BY name ASC")
    LiveData<List<Category>> getActiveCategories();

    /**
     * Get all categories (system/default categories)
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getSystemCategories();

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getSystemCategoriesSync();

    /**
     * Get all categories (kept for backward compatibility)
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getUserCustomCategories();

    /**
     * Search categories by name
     */
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<Category>> search(String query);

    /**
     * Get category by name (exact match)
     */
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getByName(String name);

    /**
     * Get most used categories (by transaction count)
     *
     * Requires JOIN with transactions table
     */
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT c.*, COUNT(t.transaction_id) as usage_count " +
           "FROM categories c " +
           "LEFT JOIN transactions t ON c.category_id = t.category_id " +
           "GROUP BY c.category_id " +
           "ORDER BY usage_count DESC " +
           "LIMIT :limit")
    LiveData<List<Category>> getMostUsedCategories(int limit);

    /**
     * Get category count
     */
    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();

    /**
     * Archive a category (soft delete by marking as inactive)
     */
    @Query("UPDATE categories SET active = 0 WHERE category_id = :categoryId")
    int archive(long categoryId);

    /**
     * Unarchive a category (mark as active)
     */
    @Query("UPDATE categories SET active = 1 WHERE category_id = :categoryId")
    int unarchive(long categoryId);

    /**
     * Check if category is being used in transactions
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE category_id = :categoryId")
    int getUsageCount(long categoryId);

    /**
     * Update category icon
     */
    @Query("UPDATE categories SET icon = :icon WHERE category_id = :categoryId")
    int updateIcon(long categoryId, String icon);

    /**
     * Update category color
     */
    @Query("UPDATE categories SET color = :color WHERE category_id = :categoryId")
    int updateColor(long categoryId, int color);
}
