package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;

/**
 * Category entity - represents transaction categories (Alimentación, Transporte, etc.)
 *
 * Categories are shared across users and can be managed by admins.
 * Some categories can be for income, others for expenses, or both.
 */
@Entity(
        tableName = "categories",
        indices = @Index(value = "name", unique = true)
)
public class Category {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    private long categoryId;

    /**
     * Category name (unique)
     */
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    /**
     * Icon identifier (drawable name or emoji)
     */
    @ColumnInfo(name = "icon")
    private String icon;

    /**
     * Category color (ARGB format)
     */
    @ColumnInfo(name = "color")
    private int color;

    /**
     * Whether this category can be used for income transactions
     */
    @ColumnInfo(name = "is_income")
    private boolean isIncome = false;

    /**
     * Whether this category can be used for expense transactions
     */
    @ColumnInfo(name = "is_expense")
    private boolean isExpense = true;

    /**
     * Display order (for sorting in UI)
     */
    @ColumnInfo(name = "display_order")
    private int displayOrder = 0;

    /**
     * Whether the category is active (inactive categories are hidden)
     */
    @ColumnInfo(name = "active")
    private boolean active = true;

    /**
     * Creation timestamp
     */
    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // ========== Constructors ==========

    public Category() {
        this.createdAt = Instant.now();
    }

    @Ignore
    public Category(@NonNull String name, String icon, int color, boolean isIncome, boolean isExpense) {
        this();
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.isIncome = isIncome;
        this.isExpense = isExpense;
    }

    // ========== Getters and Setters ==========

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }

    public boolean isExpense() {
        return isExpense;
    }

    public void setExpense(boolean expense) {
        isExpense = expense;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @NonNull
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull Instant createdAt) {
        this.createdAt = createdAt;
    }
}
