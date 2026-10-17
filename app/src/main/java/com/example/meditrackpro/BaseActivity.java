package com.example.meditrackpro;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        applyWindowInsets();
    }

    /**
     * On Android 15+ the app draws edge to edge, so content would sit under the status and
     * navigation bars. Pad the content area by the system bar sizes (and the keyboard).
     * Screens with their own bottom bar override this.
     */
    protected void applyWindowInsets() {
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(content);
    }
}
