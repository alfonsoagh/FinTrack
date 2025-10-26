package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.GroupEntity;

import java.util.List;

@Dao
public interface GroupDao {

    @Insert
    long insert(GroupEntity group);

    @Update
    void update(GroupEntity group);

    @Delete
    void delete(GroupEntity group);

    @Query("SELECT * FROM groups WHERE group_id = :groupId")
    LiveData<GroupEntity> getGroupById(long groupId);

    @Query("SELECT * FROM groups WHERE admin_user_id = :userId AND is_active = 1 LIMIT 1")
    LiveData<GroupEntity> getActiveGroupByAdminId(long userId);

    @Query("SELECT g.* FROM groups g " +
           "INNER JOIN group_members gm ON g.group_id = gm.group_id " +
           "WHERE gm.user_id = :userId AND g.is_active = 1 LIMIT 1")
    LiveData<GroupEntity> getActiveGroupByMemberId(long userId);

    @Query("SELECT * FROM groups WHERE is_active = 1")
    LiveData<List<GroupEntity>> getAllActiveGroups();
}
