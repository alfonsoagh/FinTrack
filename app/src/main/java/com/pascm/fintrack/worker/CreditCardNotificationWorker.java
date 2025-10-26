package com.pascm.fintrack.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.CreditCardDao;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.util.notifications.CreditCardNotificationHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Worker that runs daily to check for credit card statement and payment due dates
 * and sends notifications accordingly.
 */
public class CreditCardNotificationWorker extends Worker {

    private static final String TAG = "CCNotificationWorker";
    private static final int REMINDER_DAYS_BEFORE_PAYMENT = 3;

    public CreditCardNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting credit card notification check");

        try {
            Context context = getApplicationContext();
            FinTrackDatabase database = FinTrackDatabase.getDatabase(context);
            CreditCardDao creditCardDao = database.creditCardDao();
            CreditCardNotificationHelper notificationHelper = new CreditCardNotificationHelper(context);

            // Get current day of month
            Calendar calendar = Calendar.getInstance();
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

            // Calculate reminder day (3 days before payment due date)
            int reminderDay = currentDay + REMINDER_DAYS_BEFORE_PAYMENT;
            if (reminderDay > 31) {
                reminderDay = reminderDay - 31; // Wrap around to next month
            }

            Log.d(TAG, "Current day: " + currentDay);

            // Check for statement dates (fecha de corte)
            checkStatementDates(creditCardDao, notificationHelper, currentDay);

            // Check for payment due dates (fecha límite de pago)
            checkPaymentDueDates(creditCardDao, notificationHelper, currentDay);

            // Check for payment reminders (3 days before due date)
            checkPaymentReminders(creditCardDao, notificationHelper, reminderDay);

            Log.d(TAG, "Credit card notification check completed successfully");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error checking credit card notifications", e);
            return Result.retry();
        }
    }

    /**
     * Checks for cards with statement date today and sends notifications
     */
    private void checkStatementDates(CreditCardDao dao, CreditCardNotificationHelper helper, int currentDay) {
        try {
            List<CreditCardEntity> cards = dao.getCardsByStatementDay(currentDay);
            Log.d(TAG, "Found " + cards.size() + " cards with statement date on day " + currentDay);

            for (CreditCardEntity card : cards) {
                Log.d(TAG, "Sending statement notification for card: " + card.getLabel());
                helper.showStatementDateNotification(card);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking statement dates", e);
        }
    }

    /**
     * Checks for cards with payment due date today and sends notifications
     */
    private void checkPaymentDueDates(CreditCardDao dao, CreditCardNotificationHelper helper, int currentDay) {
        try {
            List<CreditCardEntity> cards = dao.getCardsByPaymentDueDay(currentDay);
            Log.d(TAG, "Found " + cards.size() + " cards with payment due date on day " + currentDay);

            for (CreditCardEntity card : cards) {
                Log.d(TAG, "Sending payment due notification for card: " + card.getLabel());
                helper.showPaymentDueNotification(card);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking payment due dates", e);
        }
    }

    /**
     * Checks for cards with payment due date in N days and sends reminder notifications
     */
    private void checkPaymentReminders(CreditCardDao dao, CreditCardNotificationHelper helper, int reminderDay) {
        try {
            List<CreditCardEntity> cards = dao.getCardsByPaymentDueDay(reminderDay);
            Log.d(TAG, "Found " + cards.size() + " cards with payment due in " + REMINDER_DAYS_BEFORE_PAYMENT + " days");

            for (CreditCardEntity card : cards) {
                Log.d(TAG, "Sending payment reminder for card: " + card.getLabel());
                helper.showPaymentReminderNotification(card, REMINDER_DAYS_BEFORE_PAYMENT);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking payment reminders", e);
        }
    }
}
