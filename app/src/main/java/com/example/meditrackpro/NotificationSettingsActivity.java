package com.example.meditrackpro;

import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class NotificationSettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        SharedPreferences prefs = SecurePrefs.get(this, PrefsConstants.NOTIF_PREFS);

        SwitchMaterial switchDose = findViewById(R.id.switchDoseReminders);
        SwitchMaterial switchMissed = findViewById(R.id.switchMissedDose);
        SwitchMaterial switchRefill = findViewById(R.id.switchRefill);
        SwitchMaterial switchCaregiver = findViewById(R.id.switchCaregiver);
        SwitchMaterial switchSound = findViewById(R.id.switchSound);

        // Load saved preferences
        switchDose.setChecked(prefs.getBoolean("dose_reminders", true));
        switchMissed.setChecked(prefs.getBoolean("missed_dose", true));
        switchRefill.setChecked(prefs.getBoolean("refill", true));
        switchCaregiver.setChecked(prefs.getBoolean("caregiver", false));
        switchSound.setChecked(prefs.getBoolean("sound", true));

        findViewById(R.id.btnSaveNotifications).setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean("dose_reminders", switchDose.isChecked())
                    .putBoolean("missed_dose", switchMissed.isChecked())
                    .putBoolean("refill", switchRefill.isChecked())
                    .putBoolean("caregiver", switchCaregiver.isChecked())
                    .putBoolean("sound", switchSound.isChecked())
                    .apply();
            Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
