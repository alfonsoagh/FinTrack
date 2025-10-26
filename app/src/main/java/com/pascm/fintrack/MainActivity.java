package com.pascm.fintrack;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.pascm.fintrack.util.notifications.NotificationPermissionHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
        }

        // Request notification permission for Android 13+
        requestNotificationPermissionIfNeeded();
    }

    /**
     * Requests notification permission if not already granted
     */
    private void requestNotificationPermissionIfNeeded() {
        if (!NotificationPermissionHelper.hasNotificationPermission(this)) {
            NotificationPermissionHelper.requestNotificationPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (NotificationPermissionHelper.handlePermissionResult(requestCode, permissions, grantResults)) {
            // Permission granted - notifications are now enabled
        } else {
            // Permission denied - user won't receive notifications
            // You could show a dialog explaining the importance of notifications
        }
    }
}