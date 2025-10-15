package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;

/**
 * UserProfile entity - stores personal information and preferences for a user.
 *
 * Separated from User table to keep authentication data distinct from profile data.
 */
@Entity(
        tableName = "user_profiles",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("user_id")
)
public class UserProfile {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "profile_id")
    private long profileId;

    /**
     * Reference to User (FK)
     */
    @ColumnInfo(name = "user_id")
    private long userId;

    /**
     * Full name
     */
    @ColumnInfo(name = "full_name")
    private String fullName;

    /**
     * Nickname or alias
     */
    @ColumnInfo(name = "alias")
    private String alias;

    /**
     * Avatar image URL (local or remote)
     */
    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    /**
     * Phone number (optional)
     */
    @ColumnInfo(name = "phone")
    private String phone;

    /**
     * Preferred language (ISO 639-1: es, en, etc.)
     */
    @NonNull
    @ColumnInfo(name = "language")
    private String language = "es";

    /**
     * UI theme preference
     */
    @NonNull
    @ColumnInfo(name = "theme")
    private Theme theme = Theme.SYSTEM;

    /**
     * Default currency code (ISO 4217: MXN, USD, EUR, etc.)
     */
    @NonNull
    @ColumnInfo(name = "default_currency")
    private String defaultCurrency = "MXN";

    /**
     * Notifications enabled
     */
    @ColumnInfo(name = "notifications_enabled")
    private boolean notificationsEnabled = true;

    /**
     * Location/GPS enabled for trips
     */
    @ColumnInfo(name = "location_enabled")
    private boolean locationEnabled = false;

    /**
     * Last modification timestamp
     */
    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    // ========== Constructors ==========

    public UserProfile() {
        this.updatedAt = Instant.now();
    }

    // ========== Getters and Setters ==========

    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @NonNull
    public String getLanguage() {
        return language;
    }

    public void setLanguage(@NonNull String language) {
        this.language = language;
    }

    @NonNull
    public Theme getTheme() {
        return theme;
    }

    public void setTheme(@NonNull Theme theme) {
        this.theme = theme;
    }

    @NonNull
    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(@NonNull String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isLocationEnabled() {
        return locationEnabled;
    }

    public void setLocationEnabled(boolean locationEnabled) {
        this.locationEnabled = locationEnabled;
    }

    @NonNull
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@NonNull Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========== Enums ==========

    public enum Theme {
        LIGHT,
        DARK,
        SYSTEM
    }
}
