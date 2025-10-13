package com.pascm.fintrack.data;

import android.content.Context;
import android.content.SharedPreferences;

public final class TripPrefs {
    private static final String PREFS_NAME = "fintrack_prefs";
    private static final String KEY_ACTIVE_TRIP = "active_trip";

    private TripPrefs() {}

    public static void setActiveTrip(Context context, boolean active) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_ACTIVE_TRIP, active).apply();
    }

    public static boolean isActiveTrip(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_ACTIVE_TRIP, false);
    }

    public static void clearAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}

