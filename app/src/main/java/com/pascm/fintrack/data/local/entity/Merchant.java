package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;
import java.util.List;

/**
 * Merchant entity - represents places/merchants where transactions occur.
 *
 * This replaces the concept of "PlacesManager" with actual persistent data.
 * Merchants can have location data and be reused across transactions.
 */
@Entity(
        tableName = "merchants",
        indices = @Index("name")
)
public class Merchant {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "merchant_id")
    private long merchantId;

    /**
     * Merchant/place name
     */
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    /**
     * Address (optional)
     */
    @ColumnInfo(name = "address")
    private String address;

    /**
     * Latitude for geolocation
     */
    @ColumnInfo(name = "latitude")
    private Double latitude;

    /**
     * Longitude for geolocation
     */
    @ColumnInfo(name = "longitude")
    private Double longitude;

    /**
     * Tags for categorization (e.g., "restaurant", "gas_station", "frequent")
     * Stored as JSON via TypeConverter
     */
    @ColumnInfo(name = "tags")
    private List<String> tags;

    /**
     * Business hours (free text or JSON format)
     */
    @ColumnInfo(name = "hours")
    private String hours;

    /**
     * Number of times this merchant has been used
     */
    @ColumnInfo(name = "usage_count")
    private int usageCount = 0;

    /**
     * Whether this is marked as a frequent place
     */
    @ColumnInfo(name = "is_frequent")
    private boolean isFrequent = false;

    /**
     * Creation timestamp
     */
    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    /**
     * Last used timestamp
     */
    @ColumnInfo(name = "last_used_at")
    private Instant lastUsedAt;

    /**
     * Photo URL (local file path)
     */
    @ColumnInfo(name = "photo_url")
    private String photoUrl;

    // ========== Constructors ==========

    public Merchant() {
        this.createdAt = Instant.now();
    }

    @Ignore
    public Merchant(@NonNull String name) {
        this();
        this.name = name;
    }

    // ========== Business Logic Methods ==========

    /**
     * Check if merchant has location data
     */
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    /**
     * Increment usage count (call when merchant is used in a transaction)
     */
    public void incrementUsage() {
        this.usageCount++;
        this.lastUsedAt = Instant.now();

        // Auto-mark as frequent after 5 uses
        if (usageCount >= 5) {
            this.isFrequent = true;
        }
    }

    // ========== Getters and Setters ==========

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public boolean isFrequent() {
        return isFrequent;
    }

    public void setFrequent(boolean frequent) {
        isFrequent = frequent;
    }

    @NonNull
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
