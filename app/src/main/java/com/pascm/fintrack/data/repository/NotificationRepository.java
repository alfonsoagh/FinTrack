package com.pascm.fintrack.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.NotificationDao;
import com.pascm.fintrack.data.local.entity.NotificationEntity;

import java.util.List;

public class NotificationRepository {

    private final NotificationDao notificationDao;

    public NotificationRepository(Context context) {
        FinTrackDatabase database = FinTrackDatabase.getDatabase(context);
        this.notificationDao = database.notificationDao();
    }

    // Get all notifications for a user
    public LiveData<List<NotificationEntity>> getAllNotifications(long userId) {
        return notificationDao.getAllNotificationsByUserId(userId);
    }

    // Get unread notifications for a user
    public LiveData<List<NotificationEntity>> getUnreadNotifications(long userId) {
        return notificationDao.getUnreadNotificationsByUserId(userId);
    }

    // Get unread notification count
    public LiveData<Integer> getUnreadCount(long userId) {
        return notificationDao.getUnreadCount(userId);
    }

    // Create a new notification
    public void createNotification(NotificationEntity notification) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            notificationDao.insert(notification);
        });
    }

    // Convenience alias: insert (matches some fragment calls)
    public void insert(NotificationEntity notification) {
        createNotification(notification);
    }

    // Mark a notification as read
    public void markAsRead(long notificationId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            notificationDao.markAsRead(notificationId);
        });
    }

    // Mark all notifications as read for a user
    public void markAllAsRead(long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            notificationDao.markAllAsRead(userId);
        });
    }

    // Delete a notification by id
    public void deleteNotification(long notificationId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            notificationDao.deleteNotification(notificationId);
        });
    }

    // Convenience overload: delete by entity
    public void deleteNotification(NotificationEntity notification) {
        if (notification == null) return;
        deleteNotification(notification.getNotificationId());
    }

    // Delete all notifications for a user
    public void deleteAllByUserId(long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            notificationDao.deleteAllByUserId(userId);
        });
    }
}
