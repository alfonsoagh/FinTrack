package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.PendingAction;
import com.pascm.fintrack.data.local.entity.SyncState;

import java.util.List;

/**
 * Data Access Object for Sync management (SyncState and PendingAction).
 *
 * Provides methods for tracking synchronization state and managing the sync queue.
 */
@Dao
public interface SyncDao {

    // ========== SyncState Operations ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSyncState(SyncState syncState);

    @Update
    int updateSyncState(SyncState syncState);

    @Delete
    int deleteSyncState(SyncState syncState);

    /**
     * Get sync state for a specific entity
     */
    @Query("SELECT * FROM sync_state WHERE entity_type = :entityType AND entity_id = :entityId LIMIT 1")
    SyncState getSyncState(String entityType, long entityId);

    /**
     * Get all entities that need syncing (dirty flag = true)
     */
    @Query("SELECT * FROM sync_state WHERE dirty_flag = 1 ORDER BY updated_at ASC")
    List<SyncState> getAllDirty();

    /**
     * Get dirty entities of a specific type
     */
    @Query("SELECT * FROM sync_state WHERE entity_type = :entityType AND dirty_flag = 1 ORDER BY updated_at ASC")
    List<SyncState> getDirtyByType(String entityType);

    /**
     * Get count of entities needing sync
     */
    @Query("SELECT COUNT(*) FROM sync_state WHERE dirty_flag = 1")
    LiveData<Integer> getDirtyCount();

    /**
     * Delete sync state by entity
     */
    @Query("DELETE FROM sync_state WHERE entity_type = :entityType AND entity_id = :entityId")
    int deleteSyncStateByEntity(String entityType, long entityId);

    /**
     * Upsert (insert or update) sync state
     */
    @Query("INSERT OR REPLACE INTO sync_state (entity_type, entity_id, firebase_id, last_synced_at, dirty_flag, pending_operation, updated_at) " +
            "VALUES (:entityType, :entityId, :firebaseId, :lastSyncedAt, :dirtyFlag, :pendingOperation, :updatedAt)")
    long upsertSyncState(String entityType, long entityId, String firebaseId, long lastSyncedAt,
                         boolean dirtyFlag, String pendingOperation, long updatedAt);

    // ========== PendingAction Operations ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPendingAction(PendingAction action);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAllPendingActions(List<PendingAction> actions);

    @Update
    int updatePendingAction(PendingAction action);

    @Delete
    int deletePendingAction(PendingAction action);

    @Query("DELETE FROM pending_actions WHERE pending_action_id = :actionId")
    int deletePendingActionById(long actionId);

    /**
     * Get all pending actions, ordered by priority (high to low) then by creation time
     */
    @Query("SELECT * FROM pending_actions ORDER BY priority DESC, created_at ASC")
    List<PendingAction> getAllPendingActions();

    /**
     * Get pending actions that are ready to retry (exponential backoff)
     */
    @Query("SELECT * FROM pending_actions WHERE retry_count < 5 ORDER BY priority DESC, created_at ASC")
    List<PendingAction> getPendingActionsReadyForRetry();

    /**
     * Get pending actions for a specific entity type
     */
    @Query("SELECT * FROM pending_actions WHERE entity_type = :entityType ORDER BY priority DESC, created_at ASC")
    List<PendingAction> getPendingActionsByType(String entityType);

    /**
     * Get pending action for specific entity
     */
    @Query("SELECT * FROM pending_actions WHERE entity_type = :entityType AND entity_id = :entityId ORDER BY created_at DESC LIMIT 1")
    PendingAction getPendingActionForEntity(String entityType, long entityId);

    /**
     * Delete pending action for specific entity
     */
    @Query("DELETE FROM pending_actions WHERE entity_type = :entityType AND entity_id = :entityId")
    int deletePendingActionForEntity(String entityType, long entityId);

    /**
     * Get count of pending actions
     */
    @Query("SELECT COUNT(*) FROM pending_actions")
    LiveData<Integer> getPendingActionCount();

    /**
     * Get actions that have exceeded max retries (failed)
     */
    @Query("SELECT * FROM pending_actions WHERE retry_count >= 5")
    List<PendingAction> getFailedActions();

    /**
     * Delete all failed actions (cleanup)
     */
    @Query("DELETE FROM pending_actions WHERE retry_count >= 5")
    int deleteFailedActions();

    /**
     * Clear all pending actions (use with caution!)
     */
    @Query("DELETE FROM pending_actions")
    int deleteAllPendingActions();

    // ========== Combined Operations ==========

    /**
     * Mark entity as dirty and create pending action
     * Should be called in a transaction
     */
    @Query("UPDATE sync_state SET dirty_flag = 1, pending_operation = :operation, updated_at = :updatedAt " +
            "WHERE entity_type = :entityType AND entity_id = :entityId")
    int markEntityDirty(String entityType, long entityId, String operation, long updatedAt);
}
