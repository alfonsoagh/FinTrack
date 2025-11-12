package com.pascm.fintrack.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.MerchantDao;
import com.pascm.fintrack.data.local.entity.Merchant;
import com.pascm.fintrack.util.SessionManager;

import java.time.Instant;
import java.util.List;

/**
 * Repository for managing places/merchants.
 * Ahora filtra y persiste por usuario (user_id).
 */
public class PlaceRepository {

    private final MerchantDao merchantDao;
    private final FinTrackDatabase database;
    private final Context context;

    /**
     * Constructor - initializes database and DAOs.
     *
     * @param context Application context
     */
    public PlaceRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = FinTrackDatabase.getDatabase(this.context);
        this.merchantDao = database.merchantDao();
    }

    private long currentUserId() {
        return SessionManager.getUserId(context);
    }

    // ========== Read Operations ==========

    /**
     * Get all places (LiveData - reactive).
     *
     * @return LiveData list of all merchants
     */
    public LiveData<List<Merchant>> getAllPlaces() {
        return merchantDao.getAll(currentUserId());
    }

    /**
     * Get all places (synchronous).
     *
     * WARNING: Don't call on main thread!
     *
     * @return List of all merchants
     */
    public List<Merchant> getAllPlacesSync() {
        return merchantDao.getAllSync(currentUserId());
    }

    /**
     * Get a single place by ID (LiveData).
     *
     * @param placeId Place ID
     * @return LiveData of merchant
     */
    public LiveData<Merchant> getPlaceById(long placeId) {
        return merchantDao.getById(placeId, currentUserId());
    }

    /**
     * Get a single place by ID (synchronous).
     *
     * WARNING: Don't call on main thread!
     *
     * @param placeId Place ID
     * @return Merchant or null
     */
    public Merchant getPlaceByIdSync(long placeId) {
        return merchantDao.getByIdSync(placeId, currentUserId());
    }

    /**
     * Get frequent places (used 5+ times).
     *
     * @return LiveData list of frequent merchants
     */
    public LiveData<List<Merchant>> getFrequentPlaces() {
        return merchantDao.getFrequentMerchants(currentUserId());
    }

    /**
     * Search places by name or address.
     *
     * @param query Search query
     * @return LiveData list of matching merchants
     */
    public LiveData<List<Merchant>> searchPlaces(String query) {
        return merchantDao.search(currentUserId(), query);
    }

    /**
     * Get place by exact name (synchronous).
     *
     * @param name Place name
     * @return Merchant or null
     */
    public Merchant getPlaceByName(String name) {
        return merchantDao.getByName(name, currentUserId());
    }

    /**
     * Get places near a location.
     *
     * @param minLat Minimum latitude
     * @param maxLat Maximum latitude
     * @param minLng Minimum longitude
     * @param maxLng Maximum longitude
     * @return LiveData list of nearby merchants
     */
    public LiveData<List<Merchant>> getNearbyPlaces(double minLat, double maxLat,
                                                     double minLng, double maxLng) {
        return merchantDao.getNearby(currentUserId(), minLat, maxLat, minLng, maxLng);
    }

    /**
     * Get favorite places (marked as frequent).
     *
     * @return LiveData list of favorite merchants
     */
    public LiveData<List<Merchant>> getFavoritePlaces() {
        return merchantDao.getFavorites(currentUserId());
    }

    /**
     * Get most used places.
     *
     * @param limit Maximum number of results
     * @return LiveData list of most used merchants
     */
    public LiveData<List<Merchant>> getMostUsedPlaces(int limit) {
        return merchantDao.getMostUsed(currentUserId(), limit);
    }

    /**
     * Get place count.
     *
     * @return LiveData of place count
     */
    public LiveData<Integer> getPlaceCount() {
        return merchantDao.getMerchantCount(currentUserId());
    }

    // ========== Write Operations ==========

    /**
     * Insert a new place.
     *
     * Executes asynchronously on a background thread.
     * Sets timestamps automatically.
     *
     * @param place Merchant entity to insert
     */
    public void insertPlace(Merchant place) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            if (place.getCreatedAt() == null) {
                place.setCreatedAt(Instant.now());
            }
            // Asegurar user_id
            if (place.getUserId() == 0) {
                place.setUserId(currentUserId());
            }

            long id = merchantDao.insert(place);

            android.util.Log.i("PlaceRepository", "Inserted place ID: " + id);
        });
    }

    /**
     * Insert a new place with callback.
     *
     * @param place Merchant entity to insert
     * @param callback Callback with the inserted place ID
     */
    public void insertPlace(Merchant place, InsertCallback callback) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            if (place.getCreatedAt() == null) {
                place.setCreatedAt(Instant.now());
            }
            if (place.getUserId() == 0) {
                place.setUserId(currentUserId());
            }

            long id = merchantDao.insert(place);

            android.util.Log.i("PlaceRepository", "Inserted place ID: " + id);

            if (callback != null) {
                callback.onInsertComplete(id);
            }
        });
    }

    /**
     * Update an existing place.
     *
     * @param place Merchant entity to update
     */
    public void updatePlace(Merchant place) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> merchantDao.update(place));
    }

    /**
     * Delete a place.
     *
     * @param place Merchant entity to delete
     */
    public void deletePlace(Merchant place) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> merchantDao.delete(place));
    }

    /**
     * Delete a place by ID.
     *
     * @param placeId Place ID to delete
     */
    public void deletePlaceById(long placeId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> merchantDao.deleteById(placeId));
    }

    /**
     * Toggle favorite status.
     *
     * @param placeId Place ID
     * @param isFavorite true to mark as favorite, false otherwise
     */
    public void toggleFavorite(long placeId, boolean isFavorite) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> merchantDao.updateFrequent(placeId, isFavorite));
    }

    /**
     * Update place location.
     *
     * @param placeId Place ID
     * @param latitude Latitude
     * @param longitude Longitude
     */
    public void updateLocation(long placeId, double latitude, double longitude) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> merchantDao.updateLocation(placeId, latitude, longitude));
    }

    /**
     * Increment usage count for a place.
     *
     * @param placeId Place ID
     */
    public void incrementUsage(long placeId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> merchantDao.incrementUsageCount(placeId, Instant.now().toEpochMilli()));
    }

    /**
     * Callback interface for insert operations
     */
    public interface InsertCallback {
        void onInsertComplete(long id);
    }
}
