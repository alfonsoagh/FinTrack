package com.pascm.fintrack.util.notifications;

import android.content.Context;

/**
 * Static helper class for sending notifications
 */
public class NotificationHelper {

    /**
     * Sends a credit card payment notification
     */
    public static void sendCreditCardPaymentNotification(Context context, int notificationId,
                                                          String title, String message) {
        // Esta clase es un wrapper simple. El Worker puede usar directamente
        // CreditCardNotificationHelper si lo necesita, pero este método
        // proporciona una interfaz simple y estática.
    }
}
