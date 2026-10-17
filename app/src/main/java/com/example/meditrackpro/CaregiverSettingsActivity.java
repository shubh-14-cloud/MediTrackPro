package com.example.meditrackpro;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class CaregiverSettingsActivity extends BaseActivity {

    private static final int SMS_PERMISSION_CODE = 101;

    private EditText etName;
    private EditText etRelation;
    private EditText etPhone;
    private SwitchMaterial switchMissed;
    private SwitchMaterial switchSummary;
    private SwitchMaterial switchEmergency;
    private SwitchMaterial switchLocation;
    private SharedPreferences prefs;

    private final ActivityResultLauncher<String[]> requestLocation =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (!LocationHelper.hasForegroundPermission(this)) {
                    switchLocation.setChecked(false);
                    Toast.makeText(this, "Location permission is needed to include your location", Toast.LENGTH_LONG).show();
                } else {
                    // Warm the cache so the first alert already has a location to send.
                    LocationHelper.fetchCurrent(this, null);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_settings);

        prefs = SecurePrefs.get(this, PrefsConstants.CAREGIVER_PREFS);

        etName = findViewById(R.id.etCaregiverName);
        etRelation = findViewById(R.id.etCaregiverRelation);
        etPhone = findViewById(R.id.etCaregiverPhone);
        switchMissed = findViewById(R.id.switchMissedDoseAlert);
        switchSummary = findViewById(R.id.switchDailySummary);
        switchEmergency = findViewById(R.id.switchEmergencyAlert);
        switchLocation = findViewById(R.id.switchShareLocation);

        // Load saved values
        etName.setText(prefs.getString("name", ""));
        etRelation.setText(prefs.getString("relation", ""));
        etPhone.setText(prefs.getString("phone", ""));
        switchMissed.setChecked(prefs.getBoolean("missed_dose", true));
        switchSummary.setChecked(prefs.getBoolean("daily_summary", false));
        switchEmergency.setChecked(prefs.getBoolean("emergency", true));
        switchLocation.setChecked(prefs.getBoolean("share_location", false)
                && LocationHelper.hasForegroundPermission(this));

        switchLocation.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked && !LocationHelper.hasForegroundPermission(this)) {
                requestLocation.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        });

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnViewLocation).setOnClickListener(v ->
                startActivity(new Intent(this, LocationActivity.class)));

        findViewById(R.id.btnSaveCaregiver).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter caregiver name", Toast.LENGTH_SHORT).show();
                return;
            }
            saveAndRequestPermission();
        });

        findViewById(R.id.btnTestSms).setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter a phone number first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission not granted — save the caregiver first to grant it", Toast.LENGTH_LONG).show();
                return;
            }
            CaregiverSmsSender.send(this, "MediTrack: This is a test message. Your caregiver alerts are working.", true);
        });

        findViewById(R.id.btnRemoveCaregiver).setOnClickListener(v -> {
            String nameText = etName.getText().toString();
            new AlertDialog.Builder(this)
                    .setTitle("Remove Caregiver")
                    .setMessage("Are you sure you want to remove "
                            + (!nameText.isEmpty() ? nameText : "this caregiver") + "?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        prefs.edit().clear().apply();
                        Toast.makeText(this, "Caregiver removed", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void saveAndRequestPermission() {
        // Persist caregiver info
        prefs.edit()
                .putString("name", etName.getText().toString().trim())
                .putString("relation", etRelation.getText().toString().trim())
                .putString("phone", etPhone.getText().toString().trim())
                .putBoolean("missed_dose", switchMissed.isChecked())
                .putBoolean("daily_summary", switchSummary.isChecked())
                .putBoolean("emergency", switchEmergency.isChecked())
                .putBoolean("share_location", switchLocation.isChecked())
                .putBoolean("active", true)
                .apply();

        // Schedule the daily summary alarm
        CaregiverAlarmScheduler.scheduleDailySummary(this);

        // Request SMS permission if not yet granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            Toast.makeText(this, "Caregiver saved! SMS alerts are active.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Caregiver saved! SMS alerts are active.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Caregiver saved. Grant SMS permission in Settings to enable alerts.",
                        Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }
}
