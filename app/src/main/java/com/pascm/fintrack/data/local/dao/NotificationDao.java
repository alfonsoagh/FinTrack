package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert
    long insert(NotificationEntity notification);

    @Update
    void update(NotificationEntity notification);

    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<NotificationEntity>> getAllNotificationsByUserId(long userId);

    @Query("SELECT * FROM notifications WHERE user_id = :userId AND is_read = 0 ORDER BY created_at DESC")
    LiveData<List<NotificationEntity>> getUnreadNotificationsByUserId(long userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = 0")
    LiveData<Integer> getUnreadCount(long userId);

    @Query("UPDATE notifications SET is_read = 1 WHERE notification_id = :notificationId")
    void markAsRead(long notificationId);

    @Query("UPDATE notifications SET is_read = 1 WHERE user_id = :userId")
    void markAllAsRead(long userId);

    @Query("DELETE FROM notifications WHERE notification_id = :notificationId")
    void deleteNotification(long notificationId);

    @Query("DELETE FROM notifications WHERE user_id = :userId")
    void deleteAllByUserId(long userId);
}
