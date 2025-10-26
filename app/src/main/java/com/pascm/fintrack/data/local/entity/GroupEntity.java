package com.pascm.fintrack.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.Instant;

@Entity(tableName = "groups")
public class GroupEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_id")
    private long groupId;

    @ColumnInfo(name = "group_name")
    private String groupName;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "admin_user_id")
    private long adminUserId;

    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    public GroupEntity() {
        this.createdAt = Instant.now();
        this.isActive = true;
    }

    // Getters and Setters
    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
