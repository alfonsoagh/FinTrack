package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;

/**
 * User entity - represents a user account in the system.
 *
 * This table stores authentication and basic user information.
 * Personal details are in UserProfile table.
 */
@Entity(
        tableName = "users",
        indices = {
                @Index(value = "email", unique = true),
                @Index(value = "firebase_uid", unique = true)
        }
)
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    private long userId;

    /**
     * Email address (unique, used for login)
     */
    @NonNull
    @ColumnInfo(name = "email")
    private String email;

    /**
     * Password hash (bcrypt/argon2). Null if using Firebase Auth only.
     */
    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    /**
     * Firebase UID from Firebase Authentication (unique)
     */
    @ColumnInfo(name = "firebase_uid")
    private String firebaseUid;

    /**
     * User account status
     */
    @NonNull
    @ColumnInfo(name = "status")
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Role ID for permissions (FK to Role table)
     */
    @ColumnInfo(name = "role_id")
    private Long roleId;

    /**
     * When the user account was created
     */
    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    /**
     * Last successful login timestamp
     */
    @ColumnInfo(name = "last_login_at")
    private Instant lastLoginAt;

    /**
     * Last modification timestamp
     */
    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    // ========== Constructors ==========

    public User() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.status = UserStatus.ACTIVE;
    }

    // ========== Getters and Setters ==========

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    @NonNull
    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(@NonNull UserStatus status) {
        this.status = status;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @NonNull
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    @NonNull
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@NonNull Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========== Enums ==========

    public enum UserStatus {
        ACTIVE,
        SUSPENDED,
        BLOCKED,
        DELETED
    }
}
