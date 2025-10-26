package com.pascm.fintrack.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.pascm.fintrack.data.TripPrefs;
import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.TripDao;
import com.pascm.fintrack.data.local.entity.Trip;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing trips.
 *
 * This repository REPLACES the old TripPrefs (SharedPreferences-based boolean flag)
 * with proper Room database persistence for complete trip management.
 *
 * Migration Guide:
 * OLD: TripPrefs.isActiveTrip(context) → BOOLEAN
 * NEW: tripRepository.hasActiveTrip(userId) → LiveData<Boolean>
 * NEW: tripRepository.getActiveTrip(userId) → LiveData<Trip>
 *
 * OLD: TripPrefs.setActiveTrip(context, true) → Create trip
 * NEW: tripRepository.createTrip(trip) + tripRepository.activateTrip(tripId)
 *
 * OLD: TripPrefs.setActiveTrip(context, false) → End trip
 * NEW: tripRepository.endTrip(tripId)
 */
public class TripRepository {

    private final TripDao tripDao;
    private final FinTrackDatabase database;

    public TripRepository(Context context) {
        this.database = FinTrackDatabase.getDatabase(context);
        this.tripDao = database.tripDao();
    }

    // ========== Read Operations ==========

    /**
     * Check if user has an active trip (replaces TripPrefs.isActiveTrip())
     *
     * @param userId User ID
     * @return LiveData<Boolean> - true if user has active trip
     */
    public LiveData<Boolean> hasActiveTrip(long userId) {
        return tripDao.hasActiveTrip(userId);
    }

    /**
     * Check if user has an active trip (synchronous)
     *
     * WARNING: Don't call on main thread!
     */
    public boolean hasActiveTripSync(long userId) {
        return tripDao.hasActiveTripSync(userId);
    }

    /**
     * Get the active trip for a user
     *
     * @param userId User ID
     * @return LiveData<Trip> - active trip or null
     */
    public LiveData<Trip> getActiveTrip(long userId) {
        return tripDao.getActiveTrip(userId);
    }

    /**
     * Get the active trip for a user (synchronous)
     */
    public Trip getActiveTripSync(long userId) {
        return tripDao.getActiveTripSync(userId);
    }

    /**
     * Get all trips for a user
     */
    public LiveData<List<Trip>> getAllTrips(long userId) {
        return tripDao.getAllByUser(userId);
    }

    /**
     * Get a specific trip by ID
     */
    public LiveData<Trip> getTripById(long tripId) {
        return tripDao.getById(tripId);
    }

    /**
     * Get upcoming trips (planned, not started yet)
     */
    public LiveData<List<Trip>> getUpcomingTrips(long userId) {
        long currentDate = LocalDate.now().toEpochDay();
        return tripDao.getUpcomingTrips(userId, currentDate);
    }

    /**
     * Get completed trips
     */
    public LiveData<List<Trip>> getCompletedTrips(long userId) {
        return tripDao.getCompletedTrips(userId);
    }

    /**
     * Get trip count
     */
    public LiveData<Integer> getTripCount(long userId) {
        return tripDao.getTripCount(userId);
    }

    // ========== Write Operations ==========

    /**
     * Create a new trip
     *
     * @param trip Trip entity
     */
    public void createTrip(Trip trip) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            Instant now = Instant.now();
            trip.setCreatedAt(now);
            trip.setUpdatedAt(now);

            long tripId = tripDao.insert(trip);

            // TODO: Mark for sync
            // SyncRepository.markForSync("TRIP", tripId, "CREATE");
        });
    }

    /**
     * Insert a new trip with callback
     *
     * @param trip Trip entity
     * @param callback Callback with generated trip ID
     */
    public void insertTrip(Trip trip, TripCallback callback) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            Instant now = Instant.now();
            trip.setCreatedAt(now);
            trip.setUpdatedAt(now);

            long tripId = tripDao.insert(trip);

            // TODO: Mark for sync
            // SyncRepository.markForSync("TRIP", tripId, "CREATE");

            if (callback != null) {
                callback.onTripInserted(tripId);
            }
        });
    }

    /**
     * Callback interface for trip insertion
     */
    public interface TripCallback {
        void onTripInserted(long tripId);
    }

    /**
     * Create a new trip and make it active immediately
     *
     * This is equivalent to the old:
     * TripPrefs.setActiveTrip(context, true)
     */
    public void createAndActivateTrip(Trip trip) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            // End any existing active trip first
            Trip existingActive = tripDao.getActiveTripSync(trip.getUserId());
            if (existingActive != null) {
                existingActive.setStatus(Trip.TripStatus.CANCELLED);
                existingActive.setUpdatedAt(Instant.now());
                tripDao.update(existingActive);
            }

            // Create new trip as active
            trip.setStatus(Trip.TripStatus.ACTIVE);
            trip.setCreatedAt(Instant.now());
            trip.setUpdatedAt(Instant.now());

            long tripId = tripDao.insert(trip);

            // TODO: Mark for sync
        });
    }

    /**
     * Update an existing trip
     */
    public void updateTrip(Trip trip) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            trip.setUpdatedAt(Instant.now());
            tripDao.update(trip);

            // TODO: Mark for sync
        });
    }

    /**
     * Activate a trip (set status to ACTIVE)
     * Deactivates any other active trip
     */
    public void activateTrip(long tripId, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            // End any existing active trip
            Trip existingActive = tripDao.getActiveTripSync(userId);
            if (existingActive != null && existingActive.getTripId() != tripId) {
                existingActive.setStatus(Trip.TripStatus.CANCELLED);
                existingActive.setUpdatedAt(Instant.now());
                tripDao.update(existingActive);
            }

            // Activate this trip
            tripDao.updateStatus(tripId, Trip.TripStatus.ACTIVE.name(), Instant.now().toEpochMilli());

            // TODO: Mark for sync
        });
    }

    /**
     * End the active trip (set status to COMPLETED)
     *
     * This replaces:
     * TripPrefs.setActiveTrip(context, false)
     */
    public void endActiveTrip(long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            Trip activeTrip = tripDao.getActiveTripSync(userId);
            if (activeTrip != null) {
                activeTrip.setStatus(Trip.TripStatus.COMPLETED);
                activeTrip.setUpdatedAt(Instant.now());
                tripDao.update(activeTrip);

                // TODO: Mark for sync
            }
        });
    }

    /**
     * End a specific trip by ID
     */
    public void endTrip(long tripId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            tripDao.updateStatus(tripId, Trip.TripStatus.COMPLETED.name(), Instant.now().toEpochMilli());

            // TODO: Mark for sync
        });
    }

    /**
     * Cancel a trip
     */
    public void cancelTrip(long tripId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            tripDao.updateStatus(tripId, Trip.TripStatus.CANCELLED.name(), Instant.now().toEpochMilli());

            // TODO: Mark for sync
        });
    }

    /**
     * Delete a trip
     */
    public void deleteTrip(Trip trip) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            tripDao.delete(trip);

            // TODO: Mark for sync
        });
    }

    // ========== Migration from TripPrefs ==========

    /**
     * Migrate trip state from old TripPrefs to Room.
     *
     * This should be called once during app upgrade.
     *
     * @param context Application context
     * @param userId  User ID
     */
    public void migrateFromTripPrefs(Context context, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            boolean hadActiveTrip = TripPrefs.isActiveTrip(context);

            if (hadActiveTrip) {
                // Create a default active trip
                Trip trip = new Trip();
                trip.setUserId(userId);
                trip.setName("Viaje Migrado");
                trip.setDescription("Viaje migrado desde versión anterior");
                trip.setStartDate(LocalDate.now());
                trip.setEndDate(LocalDate.now().plusDays(7));
                trip.setStatus(Trip.TripStatus.ACTIVE);
                trip.setCurrencyCode("MXN");
                trip.setCreatedAt(Instant.now());
                trip.setUpdatedAt(Instant.now());

                tripDao.insert(trip);

                android.util.Log.i("TripRepository", "Migrated active trip from TripPrefs");
            }

            // Clear old preferences
            TripPrefs.clearAll(context);

            android.util.Log.i("TripRepository", "Migration from TripPrefs completed");
        });
    }

    /**
     * Helper method to create a quick trip (for testing or simple use cases)
     */
    public void createQuickTrip(long userId, String destination, int durationDays) {
        Trip trip = new Trip();
        trip.setUserId(userId);
        trip.setName("Viaje a " + destination);
        trip.setDestination(destination);
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(durationDays));
        trip.setStatus(Trip.TripStatus.ACTIVE);
        trip.setCurrencyCode("MXN");

        createAndActivateTrip(trip);
    }
}
