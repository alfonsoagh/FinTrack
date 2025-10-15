package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;

/**
 * Account entity - represents a source of funds (cash, bank account, digital wallet, etc.)
 *
 * Cards (credit/debit) are linked to accounts via accountId.
 */
@Entity(
        tableName = "accounts",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("user_id")
)
public class Account {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "account_id")
    private long accountId;

    /**
     * Owner user ID (FK)
     */
    @ColumnInfo(name = "user_id")
    private long userId;

    /**
     * Account name (e.g., "Mi Cuenta de Efectivo", "Cuenta BBVA")
     */
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    /**
     * Account type
     */
    @NonNull
    @ColumnInfo(name = "type")
    private AccountType type;

    /**
     * Currency code (ISO 4217)
     */
    @NonNull
    @ColumnInfo(name = "currency_code")
    private String currencyCode = "MXN";

    /**
     * Total balance (includes pending transactions if applicable)
     */
    @ColumnInfo(name = "balance")
    private double balance = 0.0;

    /**
     * Available balance (excludes holds/pending)
     */
    @ColumnInfo(name = "available")
    private double available = 0.0;

    /**
     * Whether the account is archived (hidden from main views)
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

    public Account() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // ========== Getters and Setters ==========

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public AccountType getType() {
        return type;
    }

    public void setType(@NonNull AccountType type) {
        this.type = type;
    }

    @NonNull
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(@NonNull String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getAvailable() {
        return available;
    }

    public void setAvailable(double available) {
        this.available = available;
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

    public enum AccountType {
        CASH,               // Efectivo
        CHECKING,           // Cuenta de cheques
        SAVINGS,            // Cuenta de ahorros
        DIGITAL_WALLET,     // Billetera digital (PayPal, Mercado Pago, etc.)
        INVESTMENT,         // Cuenta de inversión
        OTHER               // Otro
    }
}
