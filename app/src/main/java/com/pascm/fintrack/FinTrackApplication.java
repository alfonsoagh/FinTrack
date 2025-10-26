package com.pascm.fintrack;

import android.app.Application;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.pascm.fintrack.worker.CardPaymentNotificationWorker;
import com.pascm.fintrack.worker.CreditCardNotificationWorker;

import java.util.concurrent.TimeUnit;

/**
 * Custom Application class for FinTrack
 * Initializes WorkManager for credit card notifications
 */
public class FinTrackApplication extends Application {

    private static final String TAG = "FinTrackApplication";
    private static final String CREDIT_CARD_NOTIFICATION_WORK = "credit_card_notification_work";
    private static final String CARD_PAYMENT_NOTIFICATION_WORK = "card_payment_notification_work";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate");

        // Initialize periodic work for credit card notifications
        scheduleCreditCardNotifications();

        // Initialize periodic work for card payment notifications
        scheduleCardPaymentNotifications();
    }

    /**
     * Schedules a daily worker to check credit card dates and send notifications
     */
    private void scheduleCreditCardNotifications() {
        Log.d(TAG, "Scheduling credit card notifications");

        // Create constraints - no network required
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresStorageNotLow(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        // Create periodic work request - runs once every 24 hours
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                CreditCardNotificationWorker.class,
                1, // repeat interval
                TimeUnit.DAYS // time unit
        )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS) // Wait 1 hour before first run
                .build();

        // Enqueue the work request
        // KEEP policy ensures we don't duplicate the work if it already exists
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                CREDIT_CARD_NOTIFICATION_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );

        Log.d(TAG, "Credit card notifications scheduled successfully");
    }

    /**
     * Schedules a daily worker to check card payment dates and send notifications
     */
    private void scheduleCardPaymentNotifications() {
        Log.d(TAG, "Scheduling card payment notifications");

        // Create constraints - no network required
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresStorageNotLow(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        // Create periodic work request - runs once every 24 hours at 9 AM (approximately)
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                CardPaymentNotificationWorker.class,
                1, // repeat interval
                TimeUnit.DAYS // time unit
        )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS) // Wait 1 hour before first run
                .build();

        // Enqueue the work request
        // KEEP policy ensures we don't duplicate the work if it already exists
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                CARD_PAYMENT_NOTIFICATION_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );

        Log.d(TAG, "Card payment notifications scheduled successfully");
    }

    /**
     * Cancels all scheduled credit card notifications
     * Can be called when user disables notifications in settings
     */
    public static void cancelCreditCardNotifications(Application application) {
        Log.d(TAG, "Cancelling credit card notifications");
        WorkManager.getInstance(application).cancelUniqueWork(CREDIT_CARD_NOTIFICATION_WORK);
    }

    /**
     * Cancels all scheduled card payment notifications
     */
    public static void cancelCardPaymentNotifications(Application application) {
        Log.d(TAG, "Cancelling card payment notifications");
        WorkManager.getInstance(application).cancelUniqueWork(CARD_PAYMENT_NOTIFICATION_WORK);
    }
}
