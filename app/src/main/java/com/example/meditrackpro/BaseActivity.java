package com.example.meditrackpro;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(@NonNull Context newBase) {
        // Apply font scale from preferences
        SharedPreferences prefs = SecurePrefs.get(newBase, PrefsConstants.APPEARANCE_PREFS);
        int sizeIndex = prefs.getInt("text_size", 1);
        Float scale = AppThemeHelper.fontScales.get(sizeIndex);
        if (scale == null) scale = 1.0f;

        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.fontScale = scale;
        Context scaledContext = newBase.createConfigurationContext(config);
        super.attachBaseContext(scaledContext);
    }
}
