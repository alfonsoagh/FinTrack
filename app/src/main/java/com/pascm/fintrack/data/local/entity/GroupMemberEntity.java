package com.pascm.fintrack.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;

@Entity(tableName = "group_members",
        foreignKeys = {
            @ForeignKey(entity = GroupEntity.class,
                    parentColumns = "group_id",
                    childColumns = "group_id",
                    onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = User.class,
                    parentColumns = "user_id",
                    childColumns = "user_id",
                    onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("group_id"), @Index("user_id")}
)
public class GroupMemberEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "member_id")
    private long memberId;

    @ColumnInfo(name = "group_id")
    private long groupId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "joined_at")
    private Instant joinedAt;

    @ColumnInfo(name = "is_admin")
    private boolean isAdmin;

    public GroupMemberEntity() {
        this.joinedAt = Instant.now();
        this.isAdmin = false;
    }

    // Getters and Setters
    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
