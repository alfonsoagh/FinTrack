package com.pascm.fintrack.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.GroupDao;
import com.pascm.fintrack.data.local.dao.GroupMemberDao;
import com.pascm.fintrack.data.local.entity.GroupEntity;
import com.pascm.fintrack.data.local.entity.GroupMemberEntity;

import java.util.List;

public class GroupRepository {

    private final GroupDao groupDao;
    private final GroupMemberDao groupMemberDao;

    public GroupRepository(Context context) {
        FinTrackDatabase database = FinTrackDatabase.getDatabase(context);
        this.groupDao = database.groupDao();
        this.groupMemberDao = database.groupMemberDao();
    }

    // ========== Group Operations ==========

    public void createGroup(GroupEntity group, OnGroupCreatedListener listener) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            long groupId = groupDao.insert(group);

            // Add creator as admin member
            GroupMemberEntity adminMember = new GroupMemberEntity();
            adminMember.setGroupId(groupId);
            adminMember.setUserId(group.getAdminUserId());
            adminMember.setAdmin(true);
            groupMemberDao.insert(adminMember);

            if (listener != null) {
                listener.onGroupCreated(groupId);
            }
        });
    }

    public LiveData<GroupEntity> getGroupById(long groupId) {
        return groupDao.getGroupById(groupId);
    }

    public LiveData<GroupEntity> getActiveGroupByAdminId(long userId) {
        return groupDao.getActiveGroupByAdminId(userId);
    }

    public LiveData<GroupEntity> getActiveGroupByMemberId(long userId) {
        return groupDao.getActiveGroupByMemberId(userId);
    }

    public void updateGroup(GroupEntity group) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> groupDao.update(group));
    }

    public void deleteGroup(GroupEntity group) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> groupDao.delete(group));
    }

    // ========== Group Member Operations ==========

    public void addMember(GroupMemberEntity member) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> groupMemberDao.insert(member));
    }

    public LiveData<List<GroupMemberEntity>> getMembersByGroupId(long groupId) {
        return groupMemberDao.getMembersByGroupId(groupId);
    }

    public LiveData<GroupMemberEntity> getMemberByGroupAndUser(long groupId, long userId) {
        return groupMemberDao.getMemberByGroupAndUser(groupId, userId);
    }

    public void removeMember(long groupId, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() ->
            groupMemberDao.removeMemberFromGroup(groupId, userId));
    }

    public LiveData<Integer> getMemberCount(long groupId) {
        return groupMemberDao.getMemberCountByGroupId(groupId);
    }

    // ========== Callbacks ==========

    public interface OnGroupCreatedListener {
        void onGroupCreated(long groupId);
    }
}
