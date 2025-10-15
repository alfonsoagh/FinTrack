package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;
import java.util.List;

/**
 * Transaction entity - represents a financial transaction (income, expense, or transfer).
 *
 * This is the core entity for tracking all financial movements in the app.
 */
@Entity(
        tableName = "transactions",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "account_id",
                        childColumns = "account_id",
                        onDelete = ForeignKey.SET_NULL
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "category_id",
                        childColumns = "category_id",
                        onDelete = ForeignKey.SET_NULL
                ),
                @ForeignKey(
                        entity = Merchant.class,
                        parentColumns = "merchant_id",
                        childColumns = "merchant_id",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index("user_id"),
                @Index("account_id"),
                @Index("card_id"),
                @Index("category_id"),
                @Index("subcategory_id"),
                @Index("merchant_id"),
                @Index("trip_id"),
                @Index("created_at"),
                @Index(value = {"user_id", "created_at"})
        }
)
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    private long transactionId;

    /**
     * Owner user ID (FK)
     */
    @ColumnInfo(name = "user_id")
    private long userId;

    /**
     * Associated account ID (FK) - can be null if using card directly
     */
    @ColumnInfo(name = "account_id")
    private Long accountId;

    /**
     * Associated card ID - soft FK to credit_cards or debit_cards
     * (Can't use ForeignKey annotation for multiple possible tables)
     */
    @ColumnInfo(name = "card_id")
    private Long cardId;

    /**
     * Card type if card_id is set ("CREDIT" or "DEBIT")
     */
    @ColumnInfo(name = "card_type")
    private String cardType;

    /**
     * Transaction amount (always positive, type determines if income or expense)
     */
    @ColumnInfo(name = "amount")
    private double amount;

    /**
     * Currency code (ISO 4217: MXN, USD, EUR, etc.)
     */
    @NonNull
    @ColumnInfo(name = "currency_code")
    private String currencyCode = "MXN";

    /**
     * Transaction type
     */
    @NonNull
    @ColumnInfo(name = "type")
    private TransactionType type;

    /**
     * Transaction status
     */
    @NonNull
    @ColumnInfo(name = "status")
    private TransactionStatus status = TransactionStatus.COMPLETED;

    /**
     * Category ID (FK)
     */
    @ColumnInfo(name = "category_id")
    private Long categoryId;

    /**
     * Subcategory ID (FK to subcategories table)
     */
    @ColumnInfo(name = "subcategory_id")
    private Long subcategoryId;

    /**
     * Merchant/Place ID (FK)
     */
    @ColumnInfo(name = "merchant_id")
    private Long merchantId;

    /**
     * Trip ID if this transaction is part of a trip (FK to trips table)
     */
    @ColumnInfo(name = "trip_id")
    private Long tripId;

    /**
     * User notes/description
     */
    @ColumnInfo(name = "notes")
    private String notes;

    /**
     * List of attachment URLs/paths (photos, receipts)
     * Stored as JSON via TypeConverter
     */
    @ColumnInfo(name = "attachments")
    private List<String> attachments;

    /**
     * Geolocation - latitude
     */
    @ColumnInfo(name = "latitude")
    private Double latitude;

    /**
     * Geolocation - longitude
     */
    @ColumnInfo(name = "longitude")
    private Double longitude;

    /**
     * When the transaction occurred (user can modify this)
     */
    @NonNull
    @ColumnInfo(name = "transaction_date")
    private Instant transactionDate;

    /**
     * When the record was created in the database
     */
    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    /**
     * Last modification timestamp
     */
    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    /**
     * Last sync timestamp with Firebase (null if never synced)
     */
    @ColumnInfo(name = "synced_at")
    private Instant syncedAt;

    // ========== Constructors ==========

    public Transaction() {
        Instant now = Instant.now();
        this.transactionDate = now;
        this.createdAt = now;
        this.updatedAt = now;
        this.type = TransactionType.EXPENSE;
        this.status = TransactionStatus.COMPLETED;
    }

    // ========== Business Logic Methods ==========

    /**
     * Get the effective amount considering transaction type.
     * Positive for income, negative for expense.
     */
    public double getEffectiveAmount() {
        return type == TransactionType.INCOME ? amount : -amount;
    }

    /**
     * Check if transaction has attachments
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    /**
     * Check if transaction has location
     */
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    /**
     * Check if transaction is part of a trip
     */
    public boolean isPartOfTrip() {
        return tripId != null;
    }

    // ========== Getters and Setters ==========

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @NonNull
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(@NonNull String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @NonNull
    public TransactionType getType() {
        return type;
    }

    public void setType(@NonNull TransactionType type) {
        this.type = type;
    }

    @NonNull
    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(@NonNull TransactionStatus status) {
        this.status = status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    public Instant getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(@NonNull Instant transactionDate) {
        this.transactionDate = transactionDate;
    }

    @NonNull
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull Instant createdAt) {
        this.createdAt = createdAt;
    }

    @NonNull
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@NonNull Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(Instant syncedAt) {
        this.syncedAt = syncedAt;
    }

    // ========== Enums ==========

    public enum TransactionType {
        INCOME,     // Ingreso
        EXPENSE,    // Gasto
        TRANSFER    // Transferencia entre cuentas
    }

    public enum TransactionStatus {
        PENDING,    // Pendiente
        COMPLETED,  // Completada
        CANCELLED   // Cancelada
    }
}
