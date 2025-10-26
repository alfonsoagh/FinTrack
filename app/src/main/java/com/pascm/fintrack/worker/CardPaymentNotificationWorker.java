package com.pascm.fintrack.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.CreditCardDao;
import com.pascm.fintrack.data.local.dao.UserDao;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.data.local.entity.User;
import com.pascm.fintrack.util.notifications.CreditCardNotificationHelper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Worker que verifica diariamente las tarjetas de crédito
 * y envía notificaciones para:
 * - Pagos próximos (3 días antes)
 * - Fecha de corte próxima (2 días antes)
 * - Pagos vencidos
 */
public class CardPaymentNotificationWorker extends Worker {

    private static final String TAG = "CardPaymentWorker";
    private final Context context;

    public CardPaymentNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting card payment notification check");

            FinTrackDatabase database = FinTrackDatabase.getDatabase(context);
            CreditCardDao creditCardDao = database.creditCardDao();
            UserDao userDao = database.userDao();

            // Obtener todos los usuarios activos
            // Por ahora verificamos tarjetas de todos los usuarios
            // TODO: filtrar por usuarios con notificaciones habilitadas
            List<Long> userIds = database.creditCardDao().getAllUserIdsSync();

            for (Long userId : userIds) {
                // Verificar si el usuario tiene notificaciones habilitadas
                // TODO: check user preferences for notifications

                // Obtener todas las tarjetas de crédito del usuario
                List<CreditCardEntity> cards = creditCardDao.getAllByUserSync(userId);

                for (CreditCardEntity card : cards) {
                    checkCardPaymentDue(card);
                    checkStatementDate(card);
                }
            }

            Log.d(TAG, "Card payment notification check completed");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error in card payment notification worker", e);
            return Result.failure();
        }
    }

    /**
     * Verifica si una tarjeta tiene un pago próximo o vencido
     */
    private void checkCardPaymentDue(CreditCardEntity card) {
        LocalDate nextPaymentDate = card.getNextPaymentDueDate();
        if (nextPaymentDate == null) return;

        LocalDate today = LocalDate.now();
        long daysUntilPayment = ChronoUnit.DAYS.between(today, nextPaymentDate);

        // Pago vencido
        if (daysUntilPayment < 0) {
            sendPaymentOverdueNotification(card, Math.abs(daysUntilPayment));
        }
        // Pago próximo (3 días antes)
        else if (daysUntilPayment <= 3 && daysUntilPayment >= 0) {
            sendPaymentDueNotification(card, daysUntilPayment);
        }
    }

    /**
     * Verifica si una tarjeta tiene la fecha de corte próxima
     */
    private void checkStatementDate(CreditCardEntity card) {
        LocalDate nextStatementDate = card.getNextStatementDate();
        if (nextStatementDate == null) return;

        LocalDate today = LocalDate.now();
        long daysUntilStatement = ChronoUnit.DAYS.between(today, nextStatementDate);

        // Fecha de corte próxima (2 días antes)
        if (daysUntilStatement <= 2 && daysUntilStatement >= 0) {
            sendStatementDateNotification(card, daysUntilStatement);
        }
    }

    /**
     * Envía notificación de pago próximo
     */
    private void sendPaymentDueNotification(CreditCardEntity card, long daysUntil) {
        CreditCardNotificationHelper notificationHelper = new CreditCardNotificationHelper(context);

        if (daysUntil == 0) {
            // Pago hoy
            notificationHelper.showPaymentDueNotification(card);
        } else {
            // Recordatorio de pago
            notificationHelper.showPaymentReminderNotification(card, (int) daysUntil);
        }

        Log.d(TAG, "Sent payment due notification for card: " + card.getLabel());
    }

    /**
     * Envía notificación de pago vencido
     */
    private void sendPaymentOverdueNotification(CreditCardEntity card, long daysOverdue) {
        // Para pagos vencidos, usar el método de pago due con mensaje urgente
        CreditCardNotificationHelper notificationHelper = new CreditCardNotificationHelper(context);
        notificationHelper.showPaymentDueNotification(card);

        Log.d(TAG, "Sent payment overdue notification for card: " + card.getLabel());
    }

    /**
     * Envía notificación de fecha de corte próxima
     */
    private void sendStatementDateNotification(CreditCardEntity card, long daysUntil) {
        if (daysUntil == 0) {
            // Fecha de corte hoy
            CreditCardNotificationHelper notificationHelper = new CreditCardNotificationHelper(context);
            notificationHelper.showStatementDateNotification(card);
            Log.d(TAG, "Sent statement date notification for card: " + card.getLabel());
        }
        // Solo notificar el día mismo de la fecha de corte
    }
}
