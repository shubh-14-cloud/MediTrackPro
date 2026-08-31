package com.example.meditrackpro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import java.util.Arrays;
import java.util.List;

public class AppAppearanceActivity extends BaseActivity {

    private String selectedTheme = "system";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_appearance);

        SharedPreferences prefs = SecurePrefs.get(this, PrefsConstants.APPEARANCE_PREFS);
        selectedTheme = prefs.getString("theme", "system");
        if (selectedTheme == null) selectedTheme = "system";

        TextView tvSizeValue = findViewById(R.id.tvTextSizeValue);
        SeekBar seekBar = findViewById(R.id.seekTextSize);
        List<String> sizeLabels = Arrays.asList("Small", "Medium", "Large");

        // Load saved seekbar position
        seekBar.setProgress(prefs.getInt("text_size", 1));
        tvSizeValue.setText(sizeLabels.get(seekBar.getProgress()));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                tvSizeValue.setText(sizeLabels.get(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
            }
        });

        // ── THEME ──
        CardView cardDark = findViewById(R.id.cardDarkTheme);
        CardView cardLight = findViewById(R.id.cardLightTheme);
        CardView cardSystem = findViewById(R.id.cardSystemTheme);

        updateThemeSelection(cardDark, cardLight, cardSystem);

        cardDark.setOnClickListener(v -> {
            selectedTheme = "dark";
            updateThemeSelection(cardDark, cardLight, cardSystem);
            Toast.makeText(this, "Dark theme selected", Toast.LENGTH_SHORT).show();
        });
        cardLight.setOnClickListener(v -> {
            selectedTheme = "light";
            updateThemeSelection(cardDark, cardLight, cardSystem);
            Toast.makeText(this, "Light theme selected", Toast.LENGTH_SHORT).show();
        });
        cardSystem.setOnClickListener(v -> {
            selectedTheme = "system";
            updateThemeSelection(cardDark, cardLight, cardSystem);
            Toast.makeText(this, "System theme selected", Toast.LENGTH_SHORT).show();
        });

        // ── BACK ──
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        // ── SAVE ──
        findViewById(R.id.btnSaveAppearance).setOnClickListener(v -> {
            prefs.edit()
                    .putInt("text_size", seekBar.getProgress())
                    .putString("theme", selectedTheme)
                    .apply();

            Toast.makeText(this, "Appearance saved! Restarting...", Toast.LENGTH_SHORT).show();

            // Restart the whole app to apply all changes
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finishAffinity();
        });
    }

    private void updateThemeSelection(CardView cardDark, CardView cardLight, CardView cardSystem) {
        int selectedAlpha = Color.argb(80, 255, 255, 255);
        int defaultAlpha = Color.argb(20, 255, 255, 255);

        cardDark.setCardBackgroundColor("dark".equals(selectedTheme) ? selectedAlpha : defaultAlpha);
        cardLight.setCardBackgroundColor("light".equals(selectedTheme) ? selectedAlpha : defaultAlpha);
        cardSystem.setCardBackgroundColor("system".equals(selectedTheme) ? selectedAlpha : defaultAlpha);
    }
}
