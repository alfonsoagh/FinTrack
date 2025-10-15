package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;

/**
 * PendingAction entity - queue of sync operations to be performed.
 *
 * When an entity is created/updated/deleted locally, a PendingAction is created
 * to track that this operation needs to be replicated to Firebase.
 *
 * This implements a reliable sync queue with retry logic.
 */
@Entity(
        tableName = "pending_actions",
        indices = {
                @Index("entity_type"),
                @Index("entity_id"),
                @Index("created_at"),
                @Index("retry_count")
        }
)
public class PendingAction {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pending_action_id")
    private long pendingActionId;

    /**
     * Type of entity (e.g., "TRANSACTION", "CREDIT_CARD", "TRIP")
     */
    @NonNull
    @ColumnInfo(name = "entity_type")
    private String entityType;

    /**
     * Local entity ID
     */
    @ColumnInfo(name = "entity_id")
    private long entityId;

    /**
     * Action to perform ("CREATE", "UPDATE", "DELETE")
     */
    @NonNull
    @ColumnInfo(name = "action")
    private String action;

    /**
     * JSON payload of the entity (snapshot at time of action)
     * This allows sync to complete even if entity is later deleted locally
     */
    @ColumnInfo(name = "payload_json")
    private String payloadJson;

    /**
     * Priority (higher = sync first)
     * Default: 0 (normal priority)
     */
    @ColumnInfo(name = "priority")
    private int priority = 0;

    /**
     * When the action was created
     */
    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    /**
     * Number of retry attempts
     */
    @ColumnInfo(name = "retry_count")
    private int retryCount = 0;

    /**
     * Last attempt timestamp
     */
    @ColumnInfo(name = "last_attempt_at")
    private Instant lastAttemptAt;

    /**
     * Last error message (if any)
     */
    @ColumnInfo(name = "last_error")
    private String lastError;

    /**
     * Whether this action requires network connectivity
     * (some actions might be processable offline)
     */
    @ColumnInfo(name = "requires_network")
    private boolean requiresNetwork = true;

    // ========== Constructors ==========

    public PendingAction() {
        this.createdAt = Instant.now();
    }

    @Ignore
    public PendingAction(@NonNull String entityType, long entityId, @NonNull String action) {
        this();
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
    }

    @Ignore
    public PendingAction(@NonNull String entityType, long entityId, @NonNull String action, String payloadJson) {
        this(entityType, entityId, action);
        this.payloadJson = payloadJson;
    }

    // ========== Business Logic Methods ==========

    /**
     * Increment retry counter and record attempt
     */
    public void recordAttempt() {
        this.retryCount++;
        this.lastAttemptAt = Instant.now();
    }

    /**
     * Record a failed attempt with error message
     */
    public void recordFailure(String error) {
        recordAttempt();
        this.lastError = error;
    }

    /**
     * Check if max retries reached (configurable, default 5)
     */
    public boolean hasExceededMaxRetries() {
        return retryCount >= 5;
    }

    /**
     * Check if should retry (exponential backoff)
     * Waits: 1min, 2min, 4min, 8min, 16min before retrying
     */
    public boolean shouldRetry() {
        if (lastAttemptAt == null) {
            return true; // First attempt
        }

        if (hasExceededMaxRetries()) {
            return false;
        }

        long minutesSinceLastAttempt = java.time.Duration.between(
                lastAttemptAt,
                Instant.now()
        ).toMinutes();

        // Exponential backoff: 2^retryCount minutes
        long backoffMinutes = (long) Math.pow(2, retryCount);

        return minutesSinceLastAttempt >= backoffMinutes;
    }

    /**
     * Check if action is a delete operation
     */
    public boolean isDeleteAction() {
        return "DELETE".equalsIgnoreCase(action);
    }

    // ========== Getters and Setters ==========

    public long getPendingActionId() {
        return pendingActionId;
    }

    public void setPendingActionId(long pendingActionId) {
        this.pendingActionId = pendingActionId;
    }

    @NonNull
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(@NonNull String entityType) {
        this.entityType = entityType;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    @NonNull
    public String getAction() {
        return action;
    }

    public void setAction(@NonNull String action) {
        this.action = action;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @NonNull
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull Instant createdAt) {
        this.createdAt = createdAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(Instant lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public boolean isRequiresNetwork() {
        return requiresNetwork;
    }

    public void setRequiresNetwork(boolean requiresNetwork) {
        this.requiresNetwork = requiresNetwork;
    }
}
