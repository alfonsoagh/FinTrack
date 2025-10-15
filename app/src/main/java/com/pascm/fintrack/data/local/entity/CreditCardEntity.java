package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.pascm.fintrack.model.CreditCard;

import java.time.Instant;

/**
 * CreditCardEntity - Room entity for credit cards.
 *
 * This replaces the non-persistent CreditCard model and includes all fields
 * necessary for tracking credit card usage, limits, and payment dates.
 *
 * Note: The existing CreditCard.java in model/ can be used as a UI model,
 * while this entity handles persistence.
 */
@Entity(
        tableName = "credit_cards",
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
public class CreditCardEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    private long cardId;

    /**
     * Owner user ID (FK)
     */
    @ColumnInfo(name = "user_id")
    private long userId;

    /**
     * Associated account ID (FK) - optional, cards can exist independently
     */
    @ColumnInfo(name = "account_id")
    private Long accountId;

    /**
     * Issuing bank/institution (e.g., "BBVA", "Santander")
     */
    @NonNull
    @ColumnInfo(name = "issuer")
    private String issuer;

    /**
     * Card label/alias (e.g., "Tarjeta Principal", "Viajes")
     */
    @NonNull
    @ColumnInfo(name = "label")
    private String label;

    /**
     * Card brand (Visa, Mastercard, Amex, etc.)
     */
    @NonNull
    @ColumnInfo(name = "brand")
    private String brand;

    /**
     * Last 4 digits of PAN (Primary Account Number)
     */
    @NonNull
    @ColumnInfo(name = "pan_last_4")
    private String panLast4;

    /**
     * Credit limit
     */
    @ColumnInfo(name = "credit_limit")
    private double creditLimit = 0.0;

    /**
     * Current balance (amount owed/used)
     */
    @ColumnInfo(name = "current_balance")
    private double currentBalance = 0.0;

    /**
     * Statement day of month (1-31)
     */
    @ColumnInfo(name = "statement_day")
    private Integer statementDay;

    /**
     * Payment due day of month (1-31)
     */
    @ColumnInfo(name = "payment_due_day")
    private Integer paymentDueDay;

    /**
     * Card gradient for UI (enum stored as string)
     */
    @NonNull
    @ColumnInfo(name = "gradient")
    private String gradient = "VIOLET";

    /**
     * Whether the card is archived (hidden)
     */
    @ColumnInfo(name = "archived")
    private boolean archived = false;

    /**
     * Creation timestamp
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

    // ========== Constructors ==========

    public CreditCardEntity() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Constructor from existing CreditCard model (for migration)
     */
    @Ignore
    public CreditCardEntity(@NonNull CreditCard card, long userId) {
        this();
        this.userId = userId;
        this.issuer = card.getBank();
        this.label = card.getLabel();
        this.brand = card.getBrand();
        this.panLast4 = card.getPanLast4();
        this.creditLimit = card.getLimit();
        this.currentBalance = card.getBalance();
        this.gradient = card.getGradient().name();
    }

    // ========== Business Logic Methods ==========

    /**
     * Calculates available credit
     */
    public double getAvailableCredit() {
        return Math.max(0, creditLimit - currentBalance);
    }

    /**
     * Calculates usage percentage (0-100)
     */
    public float getUsagePercentage() {
        if (creditLimit <= 0) return 0;
        return (float) Math.min(100, Math.max(0, (currentBalance / creditLimit) * 100));
    }

    /**
     * Determines usage level based on percentage
     */
    public UsageLevel getUsageLevel() {
        float pct = getUsagePercentage();
        if (pct < 35) return UsageLevel.LOW;
        if (pct < 70) return UsageLevel.MEDIUM;
        return UsageLevel.HIGH;
    }

    /**
     * Converts to UI model (CreditCard from model package)
     */
    @Ignore
    public CreditCard toModel() {
        CreditCard.CardGradient cardGradient;
        try {
            cardGradient = CreditCard.CardGradient.valueOf(this.gradient);
        } catch (IllegalArgumentException e) {
            cardGradient = CreditCard.CardGradient.VIOLET; // default fallback
        }

        return new CreditCard(
                this.issuer,
                this.label,
                this.brand,
                this.panLast4,
                this.creditLimit,
                this.currentBalance,
                cardGradient
        );
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
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

    @NonNull
    public String getBrand() {
        return brand;
    }

    public void setBrand(@NonNull String brand) {
        this.brand = brand;
    }

    @NonNull
    public String getPanLast4() {
        return panLast4;
    }

    public void setPanLast4(@NonNull String panLast4) {
        this.panLast4 = panLast4;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Integer getStatementDay() {
        return statementDay;
    }

    public void setStatementDay(Integer statementDay) {
        this.statementDay = statementDay;
    }

    public Integer getPaymentDueDay() {
        return paymentDueDay;
    }

    public void setPaymentDueDay(Integer paymentDueDay) {
        this.paymentDueDay = paymentDueDay;
    }

    @NonNull
    public String getGradient() {
        return gradient;
    }

    public void setGradient(@NonNull String gradient) {
        this.gradient = gradient;
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

    // ========== Enums ==========

    public enum UsageLevel {
        LOW("Bajo uso", 0xFF22C55E),      // verde
        MEDIUM("Uso medio", 0xFFF59E0B),  // amarillo/ámbar
        HIGH("Uso alto", 0xFFEF4444);     // rojo

        private final String label;
        private final int color;

        UsageLevel(String label, int color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public int getColor() {
            return color;
        }
    }
}
