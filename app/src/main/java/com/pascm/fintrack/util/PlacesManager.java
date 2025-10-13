package com.pascm.fintrack.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PlacesManager {
    private static final String PREFS_NAME = "FinTrackPrefs";
    private static final String KEY_HAS_PLACES = "has_places";

    public static void setHasPlaces(Context context, boolean hasPlaces) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_HAS_PLACES, hasPlaces).apply();
    }

    public static boolean hasPlaces(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_HAS_PLACES, false);
    }
}
