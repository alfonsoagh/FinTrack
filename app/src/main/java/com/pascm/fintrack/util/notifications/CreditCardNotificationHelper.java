package com.pascm.fintrack.util.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.pascm.fintrack.MainActivity;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.data.local.entity.NotificationEntity;
import com.pascm.fintrack.data.repository.NotificationRepository;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Helper class for creating and managing credit card notifications
 */
public class CreditCardNotificationHelper {

    private static final String CHANNEL_ID = "credit_card_notifications";
    private static final String CHANNEL_NAME = "Notificaciones de Tarjetas de Crédito";
    private static final String CHANNEL_DESCRIPTION = "Notificaciones de fechas de corte y pago de tarjetas de crédito";

    // Notification IDs
    private static final int NOTIFICATION_ID_STATEMENT_BASE = 1000;
    private static final int NOTIFICATION_ID_PAYMENT_BASE = 2000;

    private final Context context;
    private final NotificationManager notificationManager;
    private final NumberFormat currencyFormat;
    private final NotificationRepository notificationRepository;

    public CreditCardNotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        this.notificationRepository = new NotificationRepository(context);
        createNotificationChannel();
    }

    /**
     * Creates the notification channel for credit card notifications (required for Android O+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Shows a notification for statement date (fecha de corte)
     *
     * @param card The credit card entity
     */
    public void showStatementDateNotification(CreditCardEntity card) {
        if (card.getCurrentBalance() <= 0) {
            // Don't notify if there's no balance
            return;
        }

        String title = "Fecha de Corte - " + card.getLabel();
        String message = String.format(
                "Tu tarjeta %s (%s) tiene un saldo de %s. Fecha de corte hoy.",
                card.getLabel(),
                card.getIssuer(),
                currencyFormat.format(card.getCurrentBalance())
        );

        // Enviar notificación push
        showNotification(
                NOTIFICATION_ID_STATEMENT_BASE + (int) card.getCardId(),
                title,
                message,
                "Revisa el estado de tu tarjeta"
        );

        // Crear notificación interna en la base de datos
        createInternalNotification(card.getUserId(), title, message, "CARD", card.getCardId());
    }

    /**
     * Shows a notification for payment due date (fecha límite de pago)
     *
     * @param card The credit card entity
     */
    public void showPaymentDueNotification(CreditCardEntity card) {
        if (card.getCurrentBalance() <= 0) {
            // Don't notify if there's no balance
            return;
        }

        String title = "Fecha de Pago - " + card.getLabel();
        String message = String.format(
                "¡Fecha límite de pago! Tu tarjeta %s (%s) tiene un saldo de %s que debes pagar hoy.",
                card.getLabel(),
                card.getIssuer(),
                currencyFormat.format(card.getCurrentBalance())
        );

        // Enviar notificación push
        showNotification(
                NOTIFICATION_ID_PAYMENT_BASE + (int) card.getCardId(),
                title,
                message,
                "Realiza tu pago ahora"
        );

        // Crear notificación interna en la base de datos
        createInternalNotification(card.getUserId(), title, message, "CARD", card.getCardId());
    }

    /**
     * Shows a reminder notification a few days before payment due date
     *
     * @param card      The credit card entity
     * @param daysLeft  Number of days left until payment due date
     */
    public void showPaymentReminderNotification(CreditCardEntity card, int daysLeft) {
        if (card.getCurrentBalance() <= 0) {
            return;
        }

        String title = "Recordatorio de Pago - " + card.getLabel();
        String message = String.format(
                "Quedan %d días para pagar tu tarjeta %s. Saldo: %s",
                daysLeft,
                card.getLabel(),
                currencyFormat.format(card.getCurrentBalance())
        );

        // Enviar notificación push
        showNotification(
                NOTIFICATION_ID_PAYMENT_BASE + (int) card.getCardId() + 100,
                title,
                message,
                "Planifica tu pago"
        );

        // Crear notificación interna en la base de datos
        createInternalNotification(card.getUserId(), title, message, "CARD", card.getCardId());
    }

    /**
     * Internal method to show a notification
     *
     * @param notificationId Unique notification ID
     * @param title         Notification title
     * @param message       Notification message
     * @param subText       Sub-text for the notification
     */
    private void showNotification(int notificationId, String title, String message, String subText) {
        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_credit_card)
                .setContentTitle(title)
                .setContentText(message)
                .setSubText(subText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        // Show the notification
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Creates an internal notification in the database
     *
     * @param userId          The user ID
     * @param title           Notification title
     * @param message         Notification message
     * @param type            Notification type
     * @param relatedEntityId ID of related entity (card, transaction, etc.)
     */
    private void createInternalNotification(long userId, String title, String message, String type, long relatedEntityId) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRead(false);

        notificationRepository.createNotification(notification);
    }

    /**
     * Cancels all credit card notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    /**
     * Cancels a specific card's notifications
     *
     * @param cardId The card ID
     */
    public void cancelCardNotifications(long cardId) {
        notificationManager.cancel(NOTIFICATION_ID_STATEMENT_BASE + (int) cardId);
        notificationManager.cancel(NOTIFICATION_ID_PAYMENT_BASE + (int) cardId);
        notificationManager.cancel(NOTIFICATION_ID_PAYMENT_BASE + (int) cardId + 100);
    }
}
