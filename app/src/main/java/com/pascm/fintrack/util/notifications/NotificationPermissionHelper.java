package com.pascm.fintrack.util.notifications;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper class for managing notification permissions
 * Handles runtime permission requests for Android 13+ (API 33+)
 */
public class NotificationPermissionHelper {

    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    /**
     * Checks if the app has notification permission
     *
     * @param activity The activity context
     * @return true if permission is granted or not required (< Android 13)
     */
    public static boolean hasNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        // For Android versions below 13, no runtime permission is needed
        return true;
    }

    /**
     * Requests notification permission from the user
     *
     * @param activity The activity context
     */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    /**
     * Checks if we should show rationale for notification permission
     *
     * @param activity The activity context
     * @return true if we should show rationale
     */
    public static boolean shouldShowRationale(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
            );
        }
        return false;
    }

    /**
     * Handles the permission request result
     *
     * @param requestCode  The request code
     * @param permissions  The requested permissions
     * @param grantResults The grant results
     * @return true if notification permission was granted
     */
    public static boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
}
