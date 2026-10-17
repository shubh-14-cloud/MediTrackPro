package com.example.meditrackpro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;

public class AppAppearanceActivity extends BaseActivity {

    private String selectedTheme = AppThemeHelper.DEFAULT_THEME;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_appearance);

        SharedPreferences prefs = SecurePrefs.get(this, PrefsConstants.APPEARANCE_PREFS);
        selectedTheme = AppThemeHelper.getSavedTheme(this);

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
        MaterialCardView cardDark = findViewById(R.id.cardDarkTheme);
        MaterialCardView cardLight = findViewById(R.id.cardLightTheme);
        MaterialCardView cardSystem = findViewById(R.id.cardSystemTheme);

        updateThemeSelection(cardDark, cardLight, cardSystem);

        cardDark.setOnClickListener(v -> {
            selectedTheme = AppThemeHelper.THEME_DARK;
            updateThemeSelection(cardDark, cardLight, cardSystem);
        });
        cardLight.setOnClickListener(v -> {
            selectedTheme = AppThemeHelper.THEME_LIGHT;
            updateThemeSelection(cardDark, cardLight, cardSystem);
        });
        cardSystem.setOnClickListener(v -> {
            selectedTheme = AppThemeHelper.THEME_SYSTEM;
            updateThemeSelection(cardDark, cardLight, cardSystem);
        });

        // ── BACK ──
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        // ── SAVE ──
        findViewById(R.id.btnSaveAppearance).setOnClickListener(v -> {
            prefs.edit()
                    .putInt("text_size", seekBar.getProgress())
                    .putString("theme", selectedTheme)
                    .apply();

            Toast.makeText(this, "Appearance saved", Toast.LENGTH_SHORT).show();

            // Restart the whole app so the theme and text size apply everywhere
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finishAffinity();
        });
    }

    private void updateThemeSelection(MaterialCardView cardDark, MaterialCardView cardLight,
                                       MaterialCardView cardSystem) {
        UiHelper.setCardSelected(cardDark, AppThemeHelper.THEME_DARK.equals(selectedTheme));
        UiHelper.setCardSelected(cardLight, AppThemeHelper.THEME_LIGHT.equals(selectedTheme));
        UiHelper.setCardSelected(cardSystem, AppThemeHelper.THEME_SYSTEM.equals(selectedTheme));
    }
}
