package com.example.meditrackpro;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AppThemeHelper {

    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_SYSTEM = "system";

    /** The app opens in dark mode until the user picks something else. */
    public static final String DEFAULT_THEME = THEME_DARK;

    private AppThemeHelper() {
    }

    public static final Map<Integer, Float> fontScales = new LinkedHashMap<>();

    static {
        fontScales.put(0, 0.85f);
        fontScales.put(1, 1.0f);
        fontScales.put(2, 1.15f);
    }

    public static String getSavedTheme(Context context) {
        SharedPreferences prefs = SecurePrefs.get(context, PrefsConstants.APPEARANCE_PREFS);
        String theme = prefs.getString("theme", DEFAULT_THEME);
        return theme != null ? theme : DEFAULT_THEME;
    }

    public static void applyTheme(Context context) {
        int mode;
        switch (getSavedTheme(context)) {
            case THEME_LIGHT:
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case THEME_SYSTEM:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_YES;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static float getFontScale(Context context) {
        SharedPreferences prefs = SecurePrefs.get(context, PrefsConstants.APPEARANCE_PREFS);
        int sizeIndex = prefs.getInt("text_size", 1);
        Float scale = fontScales.get(sizeIndex);
        return scale != null ? scale : 1.0f;
    }
}
