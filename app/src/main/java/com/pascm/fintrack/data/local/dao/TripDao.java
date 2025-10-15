package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.Trip;

import java.util.List;

/**
 * Data Access Object for Trips.
 *
 * Provides methods for trip management including active trip tracking.
 */
@Dao
public interface TripDao {

    // ========== Insert Operations ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Trip trip);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Trip> trips);

    // ========== Update Operations ==========

    @Update
    int update(Trip trip);

    @Query("UPDATE trips SET status = :status, updated_at = :updatedAt WHERE trip_id = :tripId")
    int updateStatus(long tripId, String status, long updatedAt);

    // ========== Delete Operations ==========

    @Delete
    int delete(Trip trip);

    @Query("DELETE FROM trips WHERE trip_id = :tripId")
    int deleteById(long tripId);

    // ========== Query Operations ==========

    @Query("SELECT * FROM trips WHERE trip_id = :tripId")
    LiveData<Trip> getById(long tripId);

    @Query("SELECT * FROM trips WHERE trip_id = :tripId")
    Trip getByIdSync(long tripId);

    /**
     * Get all trips for a user, ordered by start date (newest first)
     */
    @Query("SELECT * FROM trips WHERE user_id = :userId ORDER BY start_date DESC")
    LiveData<List<Trip>> getAllByUser(long userId);

    @Query("SELECT * FROM trips WHERE user_id = :userId ORDER BY start_date DESC")
    List<Trip> getAllByUserSync(long userId);

    /**
     * Get active trip for a user (replaces TripPrefs.isActiveTrip())
     */
    @Query("SELECT * FROM trips WHERE user_id = :userId AND status = 'ACTIVE' ORDER BY start_date DESC LIMIT 1")
    LiveData<Trip> getActiveTrip(long userId);

    @Query("SELECT * FROM trips WHERE user_id = :userId AND status = 'ACTIVE' ORDER BY start_date DESC LIMIT 1")
    Trip getActiveTripSync(long userId);

    /**
     * Check if user has an active trip (returns 1 or 0)
     */
    @Query("SELECT COUNT(*) > 0 FROM trips WHERE user_id = :userId AND status = 'ACTIVE'")
    LiveData<Boolean> hasActiveTrip(long userId);

    @Query("SELECT COUNT(*) > 0 FROM trips WHERE user_id = :userId AND status = 'ACTIVE'")
    boolean hasActiveTripSync(long userId);

    /**
     * Get trips by status
     */
    @Query("SELECT * FROM trips WHERE user_id = :userId AND status = :status ORDER BY start_date DESC")
    LiveData<List<Trip>> getByStatus(long userId, String status);

    /**
     * Get upcoming trips (planned, start date in future)
     */
    @Query("SELECT * FROM trips WHERE user_id = :userId AND status = 'PLANNED' AND start_date > :currentDate ORDER BY start_date ASC")
    LiveData<List<Trip>> getUpcomingTrips(long userId, long currentDate);

    /**
     * Get completed trips
     */
    @Query("SELECT * FROM trips WHERE user_id = :userId AND status = 'COMPLETED' ORDER BY start_date DESC")
    LiveData<List<Trip>> getCompletedTrips(long userId);

    /**
     * Get trip count by status
     */
    @Query("SELECT COUNT(*) FROM trips WHERE user_id = :userId AND status = :status")
    LiveData<Integer> getTripCountByStatus(long userId, String status);

    /**
     * Get total trip count for user
     */
    @Query("SELECT COUNT(*) FROM trips WHERE user_id = :userId")
    LiveData<Integer> getTripCount(long userId);

    /**
     * Search trips by name or destination
     */
    @Query("SELECT * FROM trips WHERE user_id = :userId AND (name LIKE '%' || :query || '%' OR destination LIKE '%' || :query || '%') ORDER BY start_date DESC")
    LiveData<List<Trip>> search(long userId, String query);
}
