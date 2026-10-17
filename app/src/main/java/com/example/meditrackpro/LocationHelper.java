package com.example.meditrackpro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Tasks;

import timber.log.Timber;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Finds the patient's location and turns it into something a caregiver can open from an SMS.
 *
 * The caregiver does not need the app: alerts carry a Google Maps link. Background alerts run
 * while the app is not on screen, so the most recent fix is cached and used as a fallback.
 */
public final class LocationHelper {

    private static final String KEY_SHARE = "share_location";
    private static final String KEY_LAT = "last_lat";
    private static final String KEY_LNG = "last_lng";
    private static final String KEY_ACCURACY = "last_accuracy";
    private static final String KEY_TIME = "last_location_time";

    private static final long CURRENT_FIX_TIMEOUT_SECONDS = 10;
    private static final long LAST_FIX_TIMEOUT_SECONDS = 5;

    private LocationHelper() {
    }

    /** A location fix together with when it was taken. */
    public static final class Snapshot {
        public final double latitude;
        public final double longitude;
        public final float accuracyMeters;
        public final long timeMillis;

        Snapshot(double latitude, double longitude, float accuracyMeters, long timeMillis) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracyMeters = accuracyMeters;
            this.timeMillis = timeMillis;
        }
    }

    public interface Callback {
        /** Called on the main thread. {@code snapshot} is null if no location could be found. */
        void onLocation(@Nullable Snapshot snapshot);
    }

    // ---------------------------------------------------------------- permissions / setting

    public static boolean hasForegroundPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** True when alerts sent while the app is closed can read a fresh location. */
    public static boolean hasBackgroundPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return hasForegroundPermission(context);
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isSharingEnabled(Context context) {
        try {
            return SecurePrefs.get(context, PrefsConstants.CAREGIVER_PREFS).getBoolean(KEY_SHARE, false);
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------------------------------------------------------- cache

    @Nullable
    public static Snapshot getCached(Context context) {
        try {
            SharedPreferences prefs = SecurePrefs.get(context, PrefsConstants.CAREGIVER_PREFS);
            String lat = prefs.getString(KEY_LAT, null);
            String lng = prefs.getString(KEY_LNG, null);
            if (lat == null || lng == null) return null;
            float accuracy = prefs.getFloat(KEY_ACCURACY, 0f);
            long time = prefs.getLong(KEY_TIME, 0L);
            return new Snapshot(Double.parseDouble(lat), Double.parseDouble(lng), accuracy, time);
        } catch (Exception e) {
            Timber.w(e, "LocationHelper: could not read cached location");
            return null;
        }
    }

    private static Snapshot cache(Context context, Location location) {
        long now = System.currentTimeMillis();
        try {
            SecurePrefs.get(context, PrefsConstants.CAREGIVER_PREFS).edit()
                    .putString(KEY_LAT, Double.toString(location.getLatitude()))
                    .putString(KEY_LNG, Double.toString(location.getLongitude()))
                    .putFloat(KEY_ACCURACY, location.getAccuracy())
                    .putLong(KEY_TIME, now)
                    .apply();
        } catch (Exception e) {
            Timber.w(e, "LocationHelper: could not cache location");
        }
        return new Snapshot(location.getLatitude(), location.getLongitude(), location.getAccuracy(), now);
    }

    // ---------------------------------------------------------------- fetching

    /** Asynchronous fix for screens. The callback runs on the main thread. */
    public static void fetchCurrent(Context context, @Nullable Callback callback) {
        Context appContext = context.getApplicationContext();
        if (!hasForegroundPermission(appContext)) {
            if (callback != null) callback.onLocation(getCached(appContext));
            return;
        }

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(appContext);
        try {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Snapshot snapshot = cache(appContext, location);
                            if (callback != null) callback.onLocation(snapshot);
                        } else {
                            useLastKnown(appContext, client, callback);
                        }
                    })
                    .addOnFailureListener(e -> useLastKnown(appContext, client, callback));
        } catch (SecurityException e) {
            if (callback != null) callback.onLocation(getCached(appContext));
        }
    }

    private static void useLastKnown(Context context, FusedLocationProviderClient client, @Nullable Callback callback) {
        try {
            client.getLastLocation()
                    .addOnSuccessListener(location -> {
                        Snapshot snapshot = location != null ? cache(context, location) : getCached(context);
                        if (callback != null) callback.onLocation(snapshot);
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onLocation(getCached(context));
                    });
        } catch (SecurityException e) {
            if (callback != null) callback.onLocation(getCached(context));
        }
    }

    /**
     * Blocking fix for background alerts. Must NOT be called on the main thread.
     * Falls back to the most recent cached location when a fresh one is unavailable.
     */
    @Nullable
    @SuppressLint("MissingPermission") // guarded by hasForegroundPermission() below
    public static Snapshot fetchBlocking(Context context) {
        Context appContext = context.getApplicationContext();
        if (hasForegroundPermission(appContext)) {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(appContext);
            CancellationTokenSource cancel = new CancellationTokenSource();
            try {
                Location location = Tasks.await(
                        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancel.getToken()),
                        CURRENT_FIX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (location == null) {
                    location = Tasks.await(client.getLastLocation(), LAST_FIX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
                if (location != null) cache(appContext, location);
            } catch (Exception e) {
                cancel.cancel();
                Timber.w(e, "LocationHelper: no fresh location, using cached value");
            }
        }
        return getCached(appContext);
    }

    // ---------------------------------------------------------------- SMS text

    public static String mapsLink(Snapshot snapshot) {
        return String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f",
                snapshot.latitude, snapshot.longitude);
    }

    public static String describeAge(Snapshot snapshot) {
        long ageMinutes = Math.max(0, (System.currentTimeMillis() - snapshot.timeMillis) / 60_000L);
        if (ageMinutes < 2) return "just now";
        if (ageMinutes < 60) return ageMinutes + " min ago";
        return "as of " + DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.getDefault())
                .format(Instant.ofEpochMilli(snapshot.timeMillis).atZone(ZoneId.systemDefault()));
    }

    /**
     * Adds the patient's location to an alert, if they turned location sharing on.
     * Blocking: call from a background thread (the alarm receivers already are).
     */
    public static String appendLocation(Context context, String message) {
        if (!isSharingEnabled(context)) return message;
        Snapshot snapshot = fetchBlocking(context);
        if (snapshot == null) return message + " Location unavailable.";
        return message + " Location: " + mapsLink(snapshot) + " (" + describeAge(snapshot) + ")";
    }
}
