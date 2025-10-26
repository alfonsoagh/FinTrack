package com.pascm.fintrack.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for handling image operations.
 */
public class ImageHelper {

    private static final String TAG = "ImageHelper";
    private static final int MAX_IMAGE_SIZE = 1024; // Max width or height in pixels

    /**
     * Save image from URI to internal storage and return the file path.
     *
     * @param context  Context
     * @param imageUri URI of the image
     * @param fileName Name for the saved file
     * @return File path of saved image, or null if failed
     */
    public static String saveImageToInternalStorage(Context context, Uri imageUri, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream");
                return null;
            }

            // Decode and compress image
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap");
                return null;
            }

            // Resize if too large
            bitmap = resizeBitmap(bitmap, MAX_IMAGE_SIZE);

            // Rotate if needed (fix orientation)
            bitmap = rotateImageIfRequired(context, bitmap, imageUri);

            // Save to internal storage
            File directory = new File(context.getFilesDir(), "images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File imageFile = new File(directory, fileName);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.flush();
            outputStream.close();
            bitmap.recycle();

            Log.i(TAG, "Image saved to: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            return null;
        }
    }

    /**
     * Resize bitmap to fit within max size while maintaining aspect ratio.
     */
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(ratio * width);
        int newHeight = Math.round(ratio * height);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Rotate image if required based on EXIF data.
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap bitmap, Uri imageUri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            if (input == null) {
                return bitmap;
            }

            ExifInterface exif = new ExifInterface(input);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            input.close();

            int rotation = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }

            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data", e);
        }

        return bitmap;
    }

    /**
     * Load bitmap from file path.
     */
    public static Bitmap loadBitmapFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            Log.w(TAG, "Image file does not exist: " + path);
            return null;
        }

        return BitmapFactory.decodeFile(path);
    }

    /**
     * Delete image file.
     */
    public static boolean deleteImage(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * Save place photo to internal storage
     *
     * @param context  Context
     * @param imageUri URI of the image
     * @param placeId  Place ID for unique filename
     * @return File path of saved image, or null if failed
     */
    public static String savePlacePhoto(Context context, Uri imageUri, long placeId) {
        String fileName = "place_" + placeId + "_" + System.currentTimeMillis() + ".jpg";
        return saveImageToInternalStorage(context, imageUri, fileName);
    }

    /**
     * Save place photo from file
     *
     * @param context     Context
     * @param sourceFile  Source file
     * @param placeId     Place ID for unique filename
     * @return File path of saved image, or null if failed
     */
    public static String savePlacePhotoFromFile(Context context, File sourceFile, long placeId) {
        try {
            Uri uri = Uri.fromFile(sourceFile);
            return savePlacePhoto(context, uri, placeId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving place photo from file", e);
            return null;
        }
    }
}
