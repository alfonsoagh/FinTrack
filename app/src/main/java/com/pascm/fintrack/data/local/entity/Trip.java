package com.pascm.fintrack.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Trip entity - represents a travel/trip with budget tracking.
 *
 * This replaces TripPrefs (boolean flag) with actual trip data.
 */
@Entity(
        tableName = "trips",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("user_id"),
                @Index("start_date"),
                @Index("status"),
                @Index(value = {"user_id", "status"})
        }
)
public class Trip {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_id")
    private long tripId;

    /**
     * Owner user ID (FK)
     */
    @ColumnInfo(name = "user_id")
    private long userId;

    /**
     * Trip name/title
     */
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    /**
     * Trip description (optional)
     */
    @ColumnInfo(name = "description")
    private String description;

    /**
     * Budget amount (optional)
     */
    @ColumnInfo(name = "budget_amount")
    private Double budgetAmount;

    /**
     * Currency code for budget (ISO 4217)
     */
    @NonNull
    @ColumnInfo(name = "currency_code")
    private String currencyCode = "MXN";

    /**
     * Origin location/city name
     */
    @ColumnInfo(name = "origin")
    private String origin;

    /**
     * Origin latitude
     */
    @ColumnInfo(name = "origin_latitude")
    private Double originLatitude;

    /**
     * Origin longitude
     */
    @ColumnInfo(name = "origin_longitude")
    private Double originLongitude;

    /**
     * Destination location/city name
     */
    @ColumnInfo(name = "destination")
    private String destination;

    /**
     * Destination latitude
     */
    @ColumnInfo(name = "destination_latitude")
    private Double destinationLatitude;

    /**
     * Destination longitude
     */
    @ColumnInfo(name = "destination_longitude")
    private Double destinationLongitude;

    /**
     * Trip start date
     */
    @NonNull
    @ColumnInfo(name = "start_date")
    private LocalDate startDate;

    /**
     * Trip end date
     */
    @NonNull
    @ColumnInfo(name = "end_date")
    private LocalDate endDate;

    /**
     * Trip status
     */
    @NonNull
    @ColumnInfo(name = "status")
    private TripStatus status;

    /**
     * Additional notes
     */
    @ColumnInfo(name = "notes")
    private String notes;

    /**
     * Cover photo URL (optional)
     */
    @ColumnInfo(name = "cover_photo_url")
    private String coverPhotoUrl;

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

    public Trip() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.status = TripStatus.PLANNED;
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusDays(7);
    }

    // ========== Business Logic Methods ==========

    /**
     * Check if trip is currently active
     */
    public boolean isActive() {
        return status == TripStatus.ACTIVE;
    }

    /**
     * Check if trip is in progress (dates)
     */
    public boolean isInProgress() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Get trip duration in days
     */
    public long getDurationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Check if trip has a budget set
     */
    public boolean hasBudget() {
        return budgetAmount != null && budgetAmount > 0;
    }

    // ========== Getters and Setters ==========

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(Double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    @NonNull
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(@NonNull String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(Double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public Double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(Double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    @NonNull
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(@NonNull LocalDate startDate) {
        this.startDate = startDate;
    }

    @NonNull
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(@NonNull LocalDate endDate) {
        this.endDate = endDate;
    }

    @NonNull
    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(@NonNull TripStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCoverPhotoUrl() {
        return coverPhotoUrl;
    }

    public void setCoverPhotoUrl(String coverPhotoUrl) {
        this.coverPhotoUrl = coverPhotoUrl;
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

    public enum TripStatus {
        PLANNED,    // Planeado (futuro)
        ACTIVE,     // Activo (en progreso)
        COMPLETED,  // Completado
        CANCELLED   // Cancelado
    }
}
