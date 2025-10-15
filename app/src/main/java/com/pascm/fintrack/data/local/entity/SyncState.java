package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;

/**
 * SyncState entity - tracks synchronization state of entities with Firebase.
 *
 * This table maintains a mapping between local entities and their Firebase counterparts,
 * and tracks which entities have pending changes that need to be synced.
 */
@Entity(
        tableName = "sync_state",
        indices = {
                @Index("entity_type"),
                @Index("entity_id"),
                @Index(value = {"entity_type", "entity_id"}, unique = true),
                @Index("dirty_flag"),
                @Index("last_synced_at")
        }
)
public class SyncState {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sync_id")
    private long syncId;

    /**
     * Type of entity (e.g., "TRANSACTION", "CREDIT_CARD", "TRIP")
     */
    @NonNull
    @ColumnInfo(name = "entity_type")
    private String entityType;

    /**
     * Local entity ID (PK of the entity in its table)
     */
    @ColumnInfo(name = "entity_id")
    private long entityId;

    /**
     * Firebase document ID (from Firestore)
     * Null if entity has never been synced to Firebase
     */
    @ColumnInfo(name = "firebase_id")
    private String firebaseId;

    /**
     * Last successful sync timestamp
     * Null if never synced
     */
    @ColumnInfo(name = "last_synced_at")
    private Instant lastSyncedAt;

    /**
     * Dirty flag - true if entity has local changes not yet synced to Firebase
     */
    @ColumnInfo(name = "dirty_flag")
    private boolean dirtyFlag = false;

    /**
     * Pending operation ("CREATE", "UPDATE", "DELETE", null)
     * Used to determine what action to take when syncing
     */
    @ColumnInfo(name = "pending_operation")
    private String pendingOperation;

    /**
     * Number of sync attempts (for retry logic)
     */
    @ColumnInfo(name = "sync_attempts")
    private int syncAttempts = 0;

    /**
     * Last sync error message (if any)
     */
    @ColumnInfo(name = "last_error")
    private String lastError;

    /**
     * Last modification timestamp
     */
    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    // ========== Constructors ==========

    public SyncState() {
        this.updatedAt = Instant.now();
    }

    @Ignore
    public SyncState(@NonNull String entityType, long entityId) {
        this();
        this.entityType = entityType;
        this.entityId = entityId;
        this.dirtyFlag = true;
        this.pendingOperation = "CREATE";
    }

    @Ignore
    public SyncState(@NonNull String entityType, long entityId, String firebaseId,
                     Instant lastSyncedAt, boolean dirtyFlag, String pendingOperation) {
        this();
        this.entityType = entityType;
        this.entityId = entityId;
        this.firebaseId = firebaseId;
        this.lastSyncedAt = lastSyncedAt;
        this.dirtyFlag = dirtyFlag;
        this.pendingOperation = pendingOperation;
    }

    // ========== Business Logic Methods ==========

    /**
     * Mark this entity as synced successfully
     */
    public void markSynced(String firebaseId) {
        this.firebaseId = firebaseId;
        this.lastSyncedAt = Instant.now();
        this.dirtyFlag = false;
        this.pendingOperation = null;
        this.syncAttempts = 0;
        this.lastError = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Mark this entity as dirty (needs sync)
     */
    public void markDirty(String operation) {
        this.dirtyFlag = true;
        this.pendingOperation = operation;
        this.updatedAt = Instant.now();
    }

    /**
     * Increment sync attempt counter
     */
    public void incrementAttempts() {
        this.syncAttempts++;
        this.updatedAt = Instant.now();
    }

    /**
     * Record a sync error
     */
    public void recordError(String error) {
        this.lastError = error;
        this.updatedAt = Instant.now();
    }

    /**
     * Check if entity needs sync
     */
    public boolean needsSync() {
        return dirtyFlag && pendingOperation != null;
    }

    /**
     * Check if entity has been synced before
     */
    public boolean hasBeenSynced() {
        return firebaseId != null && lastSyncedAt != null;
    }

    // ========== Getters and Setters ==========

    public long getSyncId() {
        return syncId;
    }

    public void setSyncId(long syncId) {
        this.syncId = syncId;
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

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public boolean isDirtyFlag() {
        return dirtyFlag;
    }

    public void setDirtyFlag(boolean dirtyFlag) {
        this.dirtyFlag = dirtyFlag;
    }

    public String getPendingOperation() {
        return pendingOperation;
    }

    public void setPendingOperation(String pendingOperation) {
        this.pendingOperation = pendingOperation;
    }

    public int getSyncAttempts() {
        return syncAttempts;
    }

    public void setSyncAttempts(int syncAttempts) {
        this.syncAttempts = syncAttempts;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    @NonNull
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@NonNull Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
