package com.example.meditrackpro;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AppThemeHelper {

    private AppThemeHelper() {
    }

    public static final Map<Integer, Float> fontScales = new LinkedHashMap<>();

    static {
        fontScales.put(0, 0.85f);
        fontScales.put(1, 1.0f);
        fontScales.put(2, 1.15f);
    }

    public static void applyTheme(Context context) {
        SharedPreferences prefs = SecurePrefs.get(context, PrefsConstants.APPEARANCE_PREFS);
        String theme = prefs.getString("theme", "system");
        if (theme == null) theme = "system";

        int mode;
        switch (theme) {
            case "dark":
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case "light":
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static float getFontScale(Context context) {
        SharedPreferences prefs = SecurePrefs.get(context, PrefsConstants.APPEARANCE_PREFS);
        int sizeIndex = prefs.getInt("text_size", 1);
        Float scale = fontScales.get(sizeIndex);
        return scale != null ? scale : 1.0f;
    }

    public static void applyAccentToBottomNav(Context context, BottomNavigationView bottomNav) {
        int white = Color.WHITE;
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{white, Color.argb(128, 255, 255, 255)};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        bottomNav.setItemIconTintList(colorStateList);
        bottomNav.setItemTextColor(colorStateList);
    }
}
