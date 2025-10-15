package com.pascm.fintrack.data.local.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Type converters for Room database to handle complex data types.
 *
 * Room cannot directly persist complex types like List, Map, Instant, or LocalDate.
 * These converters transform them to/from simple types (String, Long) that Room can handle.
 */
public class Converters {

    private static final Gson gson = new Gson();

    // ========== Instant (timestamps) ==========

    /**
     * Converts Instant to Long (epoch millis) for database storage.
     */
    @TypeConverter
    public static Long instantToLong(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    /**
     * Converts Long (epoch millis) back to Instant from database.
     */
    @TypeConverter
    public static Instant longToInstant(Long value) {
        return value == null ? null : Instant.ofEpochMilli(value);
    }

    // ========== LocalDate ==========

    /**
     * Converts LocalDate to Long (epoch day) for database storage.
     */
    @TypeConverter
    public static Long localDateToLong(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }

    /**
     * Converts Long (epoch day) back to LocalDate from database.
     */
    @TypeConverter
    public static LocalDate longToLocalDate(Long value) {
        return value == null ? null : LocalDate.ofEpochDay(value);
    }

    // ========== List<String> ==========

    /**
     * Converts List<String> to JSON string for database storage.
     */
    @TypeConverter
    public static String stringListToJson(List<String> list) {
        if (list == null) return null;
        return gson.toJson(list);
    }

    /**
     * Converts JSON string back to List<String> from database.
     */
    @TypeConverter
    public static List<String> jsonToStringList(String json) {
        if (json == null) return null;
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    // ========== List<Long> (for IDs, permission lists, etc.) ==========

    /**
     * Converts List<Long> to JSON string for database storage.
     */
    @TypeConverter
    public static String longListToJson(List<Long> list) {
        if (list == null) return null;
        return gson.toJson(list);
    }

    /**
     * Converts JSON string back to List<Long> from database.
     */
    @TypeConverter
    public static List<Long> jsonToLongList(String json) {
        if (json == null) return null;
        Type listType = new TypeToken<ArrayList<Long>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    // ========== Map<String, String> (for metadata, payloads) ==========

    /**
     * Converts Map<String, String> to JSON string for database storage.
     */
    @TypeConverter
    public static String mapToJson(Map<String, String> map) {
        if (map == null) return null;
        return gson.toJson(map);
    }

    /**
     * Converts JSON string back to Map<String, String> from database.
     */
    @TypeConverter
    public static Map<String, String> jsonToMap(String json) {
        if (json == null) return null;
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(json, mapType);
    }

    // ========== Map<String, Object> (for flexible metadata/JSON payloads) ==========

    /**
     * Converts Map<String, Object> to JSON string for database storage.
     */
    @TypeConverter
    public static String objectMapToJson(Map<String, Object> map) {
        if (map == null) return null;
        return gson.toJson(map);
    }

    /**
     * Converts JSON string back to Map<String, Object> from database.
     */
    @TypeConverter
    public static Map<String, Object> jsonToObjectMap(String json) {
        if (json == null) return null;
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(json, mapType);
    }

    // ========== Double (for proper null handling) ==========

    /**
     * Converts Double to Double (null-safe, used when needed explicitly).
     */
    @TypeConverter
    public static Double doubleToDouble(Double value) {
        return value;
    }
}
