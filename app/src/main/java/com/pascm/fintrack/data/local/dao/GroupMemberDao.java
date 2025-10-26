package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.GroupMemberEntity;

import java.util.List;

@Dao
public interface GroupMemberDao {

    @Insert
    long insert(GroupMemberEntity member);

    @Update
    void update(GroupMemberEntity member);

    @Delete
    void delete(GroupMemberEntity member);

    @Query("SELECT * FROM group_members WHERE group_id = :groupId")
    LiveData<List<GroupMemberEntity>> getMembersByGroupId(long groupId);

    @Query("SELECT * FROM group_members WHERE user_id = :userId")
    LiveData<List<GroupMemberEntity>> getGroupsByUserId(long userId);

    @Query("SELECT * FROM group_members WHERE group_id = :groupId AND user_id = :userId")
    LiveData<GroupMemberEntity> getMemberByGroupAndUser(long groupId, long userId);

    @Query("DELETE FROM group_members WHERE group_id = :groupId AND user_id = :userId")
    void removeMemberFromGroup(long groupId, long userId);

    @Query("SELECT COUNT(*) FROM group_members WHERE group_id = :groupId")
    LiveData<Integer> getMemberCountByGroupId(long groupId);
}
