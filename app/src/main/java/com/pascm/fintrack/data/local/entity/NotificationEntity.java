package com.pascm.fintrack.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;

@Entity(
        tableName = "notifications",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "user_id"),
                @Index(value = "created_at")
        }
)
public class NotificationEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    private long notificationId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "type")
    private String type; // EXPENSE, CARD, GROUP, TRIP, GENERAL

    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    @ColumnInfo(name = "is_read")
    private boolean isRead;

    @ColumnInfo(name = "related_entity_id")
    private Long relatedEntityId; // Optional: ID of related transaction, card, etc.

    // Constructors
    public NotificationEntity() {
        this.createdAt = Instant.now();
        this.isRead = false;
    }

    // Getters and Setters
    public long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
}
