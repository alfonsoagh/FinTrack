package com.pascm.fintrack.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardsManager {
    private static final String PREFS_NAME = "FinTrackPrefs";
    private static final String KEY_CREDIT = "cards_credit";
    private static final String KEY_DEBIT = "cards_debit";

    public static final String TYPE_CREDIT = "credit";
    public static final String TYPE_DEBIT = "debit";

    private static String keyForType(String type) {
        return TYPE_CREDIT.equals(type) ? KEY_CREDIT : KEY_DEBIT;
    }

    public static List<String> getCards(Context context, String type) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(keyForType(type), "");
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        String[] parts = raw.split("\u0001");
        return new ArrayList<>(Arrays.asList(parts));
    }

    public static void addCard(Context context, String type, String label) {
        if (label == null) return;
        label = label.trim();
        if (label.isEmpty()) return;
        List<String> cards = getCards(context, type);
        cards.add(label);
        saveCards(context, type, cards);
    }

    public static void saveCards(Context context, String type, List<String> cards) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String raw = String.join("\u0001", cards);
        prefs.edit().putString(keyForType(type), raw).apply();
    }
}

