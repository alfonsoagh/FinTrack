package com.pascm.fintrack.model;

/**
 * Model class for category-based reports
 */
public class CategoryReport {
    private long categoryId;
    private String categoryName;
    private String categoryIcon;
    private int categoryColor;
    private double amount;
    private int transactionCount;
    private double percentage;

    public CategoryReport() {
    }

    public CategoryReport(long categoryId, String categoryName, String categoryIcon,
                          int categoryColor, double amount, int transactionCount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.categoryColor = categoryColor;
        this.amount = amount;
        this.transactionCount = transactionCount;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public int getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(int categoryColor) {
        this.categoryColor = categoryColor;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
