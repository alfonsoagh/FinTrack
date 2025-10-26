package com.pascm.fintrack.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper class para manejar permisos de ubicación de alta precisión
 *
 * Uso:
 * 1. Verificar si tiene permisos: hasLocationPermission()
 * 2. Solicitar permisos: requestLocationPermission()
 * 3. Manejar resultado en onRequestPermissionsResult()
 *
 * Soporta:
 * - ACCESS_FINE_LOCATION (ubicación precisa GPS)
 * - ACCESS_COARSE_LOCATION (ubicación aproximada red)
 * - ACCESS_BACKGROUND_LOCATION (ubicación en segundo plano Android 10+)
 */
public class LocationPermissionHelper {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    public static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002;

    /**
     * Verifica si la app tiene permisos de ubicación precisa (FINE_LOCATION)
     * Esta es la ubicación de ALTA PRECISIÓN que necesitas para GPS exacto
     */
    public static boolean hasFineLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica si la app tiene permisos de ubicación aproximada (COARSE_LOCATION)
     */
    public static boolean hasCoarseLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica si tiene al menos un permiso de ubicación (fine o coarse)
     */
    public static boolean hasAnyLocationPermission(Context context) {
        return hasFineLocationPermission(context) || hasCoarseLocationPermission(context);
    }

    /**
     * Verifica si tiene permiso de ubicación en segundo plano (Android 10+)
     * Solo disponible en Android 10 (API 29) o superior
     */
    public static boolean hasBackgroundLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        // En versiones anteriores a Android 10, no se necesita permiso separado
        return true;
    }

    /**
     * Solicita permisos de ubicación de ALTA PRECISIÓN (FINE + COARSE)
     * Esta es la función principal que debes usar para obtener la ubicación más exacta
     *
     * @param activity La actividad desde donde se solicita el permiso
     */
    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
            activity,
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    /**
     * Solicita permiso de ubicación en segundo plano (solo Android 10+)
     * Debe llamarse DESPUÉS de haber obtenido los permisos foreground
     *
     * @param activity La actividad desde donde se solicita el permiso
     */
    public static void requestBackgroundLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    /**
     * Verifica si debemos mostrar una explicación al usuario sobre por qué necesitamos el permiso
     * Esto es útil cuando el usuario ha denegado el permiso previamente
     *
     * @param activity La actividad donde se mostrará la explicación
     * @return true si debemos mostrar una explicación
     */
    public static boolean shouldShowLocationRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        );
    }

    /**
     * Verifica si debemos mostrar explicación para ubicación en segundo plano
     */
    public static boolean shouldShowBackgroundLocationRationale(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            );
        }
        return false;
    }

    /**
     * Maneja el resultado de la solicitud de permisos
     * Llama a este método desde onRequestPermissionsResult() en tu Activity/Fragment
     *
     * @param requestCode El código de solicitud
     * @param grantResults Los resultados de los permisos
     * @return true si el permiso fue concedido, false si fue denegado
     */
    public static boolean handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            return grantResults.length > 0 &&
                   grantResults[0] == PackageManager.PERMISSION_GRANTED;
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            return grantResults.length > 0 &&
                   grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    /**
     * Obtiene un mensaje de texto explicativo según el estado del permiso
     * Útil para mostrar al usuario por qué necesitamos el permiso
     */
    public static String getLocationPermissionExplanation(Context context) {
        return "FinTrack necesita acceso a tu ubicación para:\n\n" +
               "• Guardar automáticamente la ubicación de tus viajes\n" +
               "• Asociar transacciones con lugares específicos\n" +
               "• Mostrarte tus gastos en el mapa\n" +
               "• Ayudarte a recordar dónde realizaste cada gasto\n\n" +
               "Tu ubicación solo se usa cuando estás usando la app y nunca se comparte con terceros.";
    }

    /**
     * Verifica el mejor nivel de precisión disponible según los permisos concedidos
     *
     * @return "HIGH" si tiene FINE, "MEDIUM" si tiene COARSE, "NONE" si no tiene ninguno
     */
    public static String getLocationAccuracyLevel(Context context) {
        if (hasFineLocationPermission(context)) {
            return "HIGH"; // GPS de alta precisión (5-10 metros)
        } else if (hasCoarseLocationPermission(context)) {
            return "MEDIUM"; // Red Wi-Fi/celular (100-500 metros)
        } else {
            return "NONE";
        }
    }

    /**
     * Verifica si estamos en Android 10 o superior
     * (donde se requiere permiso separado para ubicación en segundo plano)
     */
    public static boolean isAndroid10OrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    /**
     * Verifica si todos los permisos necesarios están concedidos
     * Incluye background si estamos en Android 10+
     */
    public static boolean hasAllLocationPermissions(Context context) {
        boolean hasForeground = hasFineLocationPermission(context);
        boolean hasBackground = hasBackgroundLocationPermission(context);
        return hasForeground && hasBackground;
    }
}
