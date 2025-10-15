package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;

/**
 * Room entity for Debit Cards.
 *
 * Represents a physical or virtual debit card linked to a bank account.
 * Unlike credit cards, debit cards directly draw from an account balance.
 *
 * Differences from CreditCardEntity:
 * - No credit limit (uses account balance instead)
 * - No current balance tracking (tracked in linked Account)
 * - No statement/payment dates
 * - Must be linked to an Account
 *
 * Usage Example:
 * <pre>
 * DebitCardEntity card = new DebitCardEntity();
 * card.setUserId(1L);
 * card.setAccountId(accountId);
 * card.setIssuer("Banco Nacional");
 * card.setLabel("Tarjeta Nómina");
 * card.setBrand("VISA");
 * card.setPanLast4("4532");
 * card.setGradient("GRADIENT_BLUE");
 * debitCardDao.insert(card);
 * </pre>
 */
@Entity(
    tableName = "debit_cards",
    foreignKeys = {
        @ForeignKey(
            entity = Account.class,
            parentColumns = "account_id",
            childColumns = "account_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = User.class,
            parentColumns = "user_id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("user_id"),
        @Index("account_id")
    }
)
public class DebitCardEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    private long cardId;

    /**
     * User who owns this card
     */
    @NonNull
    @ColumnInfo(name = "user_id")
    private long userId;

    /**
     * Linked account (REQUIRED for debit cards)
     *
     * When a transaction is made with this debit card,
     * it should update the balance of this account.
     */
    @NonNull
    @ColumnInfo(name = "account_id")
    private long accountId;

    /**
     * Card issuer (bank name)
     *
     * Examples: "BBVA", "Santander", "Banamex", "Banorte"
     */
    @NonNull
    @ColumnInfo(name = "issuer")
    private String issuer;

    /**
     * User-defined label for the card
     *
     * Examples: "Tarjeta Nómina", "Débito Principal", "Cuenta de Ahorro"
     */
    @NonNull
    @ColumnInfo(name = "label")
    private String label;

    /**
     * Card brand/network
     *
     * Examples: "VISA", "MASTERCARD", "AMEX"
     */
    @ColumnInfo(name = "brand")
    private String brand;

    /**
     * Last 4 digits of card PAN (Primary Account Number)
     *
     * Example: "4532" for card ending in 4532
     * Used for display purposes only (e.g., "**** **** **** 4532")
     */
    @ColumnInfo(name = "pan_last4")
    private String panLast4;

    /**
     * Card type (physical or virtual)
     */
    @NonNull
    @ColumnInfo(name = "card_type")
    private CardType cardType = CardType.PHYSICAL;

    /**
     * Expiration date (stored as epoch milliseconds)
     *
     * Can be used to remind user when card is about to expire
     */
    @ColumnInfo(name = "expiry_date")
    private Instant expiryDate;

    /**
     * Daily spending limit (optional)
     *
     * Some banks allow setting a daily limit for debit card transactions
     */
    @ColumnInfo(name = "daily_limit")
    private Double dailyLimit;

    /**
     * Gradient name for UI card display
     *
     * Should match enum in DebitCard UI model
     * Examples: "GRADIENT_BLUE", "GRADIENT_GREEN", "GRADIENT_PURPLE"
     */
    @NonNull
    @ColumnInfo(name = "gradient")
    private String gradient = "GRADIENT_BLUE";

    /**
     * Is this card the primary/default debit card?
     */
    @NonNull
    @ColumnInfo(name = "is_primary")
    private boolean isPrimary = false;

    /**
     * Is this card active or blocked?
     */
    @NonNull
    @ColumnInfo(name = "is_active")
    private boolean isActive = true;

    /**
     * Is this card archived (soft delete)?
     */
    @NonNull
    @ColumnInfo(name = "archived")
    private boolean archived = false;

    /**
     * Timestamp of creation
     */
    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    /**
     * Timestamp of last update
     */
    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    /**
     * Firebase document ID (for sync)
     */
    @ColumnInfo(name = "firebase_id")
    private String firebaseId;

    // ========== Enums ==========

    public enum CardType {
        PHYSICAL,   // Physical plastic card
        VIRTUAL     // Virtual/digital card
    }

    // ========== Constructors ==========

    public DebitCardEntity() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @Ignore
    public DebitCardEntity(long userId, long accountId, @NonNull String issuer, @NonNull String label) {
        this();
        this.userId = userId;
        this.accountId = accountId;
        this.issuer = issuer;
        this.label = label;
    }

    // ========== Business Logic Methods ==========

    /**
     * Check if card is expired
     */
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return Instant.now().isAfter(expiryDate);
    }

    /**
     * Check if card is expiring soon (within N days)
     */
    public boolean isExpiringSoon(int days) {
        if (expiryDate == null) return false;
        Instant threshold = Instant.now().plusSeconds(days * 86400L);
        return expiryDate.isBefore(threshold);
    }

    /**
     * Get display name (label + last 4 digits)
     */
    public String getDisplayName() {
        if (panLast4 != null && !panLast4.isEmpty()) {
            return label + " (" + panLast4 + ")";
        }
        return label;
    }

    /**
     * Get masked card number for display
     */
    public String getMaskedCardNumber() {
        if (panLast4 != null && !panLast4.isEmpty()) {
            return "**** **** **** " + panLast4;
        }
        return "**** **** **** ****";
    }

    // ========== Getters and Setters ==========

    public long getCardId() {
        return cardId;
    }

    public void setCardId(long cardId) {
        this.cardId = cardId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @NonNull
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(@NonNull String issuer) {
        this.issuer = issuer;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    public void setLabel(@NonNull String label) {
        this.label = label;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPanLast4() {
        return panLast4;
    }

    public void setPanLast4(String panLast4) {
        this.panLast4 = panLast4;
    }

    @NonNull
    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(@NonNull CardType cardType) {
        this.cardType = cardType;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Double getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(Double dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    @NonNull
    public String getGradient() {
        return gradient;
    }

    public void setGradient(@NonNull String gradient) {
        this.gradient = gradient;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
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

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    // ========== toString ==========

    @Override
    public String toString() {
        return "DebitCardEntity{" +
                "cardId=" + cardId +
                ", userId=" + userId +
                ", accountId=" + accountId +
                ", issuer='" + issuer + '\'' +
                ", label='" + label + '\'' +
                ", brand='" + brand + '\'' +
                ", panLast4='" + panLast4 + '\'' +
                ", cardType=" + cardType +
                ", isActive=" + isActive +
                ", archived=" + archived +
                '}';
    }
}
