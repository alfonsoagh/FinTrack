package com.pascm.fintrack.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.pascm.fintrack.data.local.entity.User;

/**
 * Session manager for storing logged-in user information.
 *
 * Uses SharedPreferences to persist the current user session.
 * This is a simple local session - no tokens or Firebase session management.
 *
 * Usage:
 * <pre>
 * // After successful login
 * SessionManager.login(context, user);
 *
 * // Check if logged in
 * if (SessionManager.isLoggedIn(context)) {
 *     long userId = SessionManager.getUserId(context);
 * }
 *
 * // Logout
 * SessionManager.logout(context);
 * </pre>
 */
public class SessionManager {

    private static final String PREF_NAME = "FinTrackSession";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    /**
     * Save user login session.
     *
     * @param context Context
     * @param user    User who logged in
     */
    public static void login(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.apply();

        android.util.Log.i("SessionManager", "User logged in: " + user.getEmail() + " (ID: " + user.getUserId() + ")");
    }

    /**
     * Save user login session with name.
     *
     * @param context  Context
     * @param user     User who logged in
     * @param fullName User's full name
     */
    public static void login(Context context, User user, String fullName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, fullName);
        editor.apply();

        android.util.Log.i("SessionManager", "User logged in: " + user.getEmail() + " (ID: " + user.getUserId() + ")");
    }

    /**
     * Clear user session (logout).
     *
     * @param context Context
     */
    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String email = prefs.getString(KEY_USER_EMAIL, "unknown");

        editor.clear();
        editor.apply();

        android.util.Log.i("SessionManager", "User logged out: " + email);
    }

    /**
     * Check if user is logged in.
     *
     * @param context Context
     * @return true if logged in
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get logged-in user ID.
     *
     * @param context Context
     * @return User ID, or -1 if not logged in
     */
    public static long getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_USER_ID, -1);
    }

    /**
     * Get logged-in user email.
     *
     * @param context Context
     * @return User email, or null if not logged in
     */
    public static String getUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Get logged-in user name.
     *
     * @param context Context
     * @return User name, or null if not set
     */
    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, null);
    }

    /**
     * Require login - throw exception if not logged in.
     *
     * Use this in fragments/activities that require authentication.
     *
     * @param context Context
     * @return User ID
     * @throws IllegalStateException if not logged in
     */
    public static long requireUserId(Context context) {
        if (!isLoggedIn(context)) {
            throw new IllegalStateException("User not logged in");
        }
        return getUserId(context);
    }
}
