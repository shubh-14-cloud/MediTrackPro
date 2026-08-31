package com.example.meditrackpro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class PrivacySettingsActivity extends BaseActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        auth = FirebaseAuth.getInstance();

        SharedPreferences prefs = SecurePrefs.get(this, PrefsConstants.PRIVACY_PREFS);
        Switch switchAnalytics = findViewById(R.id.switchAnalytics);
        Switch switchBiometric = findViewById(R.id.switchBiometric);
        Switch switchBackup = findViewById(R.id.switchBackup);

        // Load saved prefs
        switchAnalytics.setChecked(prefs.getBoolean("analytics", true));
        switchBiometric.setChecked(prefs.getBoolean("biometric", false));
        switchBackup.setChecked(prefs.getBoolean("backup", true));

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        // Cloud backup toggle — not yet implemented
        switchBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Cloud backup coming in a future update", Toast.LENGTH_SHORT).show();
                switchBackup.setChecked(false);
            }
        });

        // Biometric toggle
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                BiometricManager biometricManager = BiometricManager.from(this);
                int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
                if (result == BiometricManager.BIOMETRIC_SUCCESS) {
                    Toast.makeText(this, "Biometric lock enabled!", Toast.LENGTH_SHORT).show();
                    prefs.edit().putBoolean("biometric", true).apply();
                } else if (result == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
                    Toast.makeText(this, "No biometric hardware found", Toast.LENGTH_SHORT).show();
                    switchBiometric.setChecked(false);
                } else if (result == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                    Toast.makeText(this, "No fingerprints enrolled. Please set up in device settings.", Toast.LENGTH_LONG).show();
                    switchBiometric.setChecked(false);
                } else {
                    Toast.makeText(this, "Biometric not available", Toast.LENGTH_SHORT).show();
                    switchBiometric.setChecked(false);
                }
            } else {
                prefs.edit().putBoolean("biometric", false).apply();
                Toast.makeText(this, "Biometric lock disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear all health data
        findViewById(R.id.btnClearData).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Clear All Health Data")
                        .setMessage("This will permanently delete all your medicines from this device. This cannot be undone.")
                        .setPositiveButton("Clear All", (dialog, which) -> {
                            MedicineDatabase.databaseWriteExecutor.execute(() ->
                                    MedicineDatabase.getDatabase(getApplicationContext())
                                            .medicineDao()
                                            .clearAllMedicines());
                            // Clear all SharedPreferences
                            List<String> prefNames = Arrays.asList(
                                    PrefsConstants.NOTIF_PREFS, PrefsConstants.PRIVACY_PREFS,
                                    PrefsConstants.EMERGENCY_PREFS, PrefsConstants.APPEARANCE_PREFS);
                            for (String prefName : prefNames) {
                                SecurePrefs.get(this, prefName).edit().clear().apply();
                            }
                            Toast.makeText(this, "All health data cleared!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show());

        // Delete account
        findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> showDeleteAccountDialog());

        // Save button
        findViewById(R.id.btnSavePrivacy).setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean("analytics", switchAnalytics.isChecked())
                    .putBoolean("biometric", switchBiometric.isChecked())
                    .putBoolean("backup", switchBackup.isChecked())
                    .apply();
            Toast.makeText(this, "Privacy settings saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showDeleteAccountDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        EditText input = new EditText(this);
        input.setHint("Enter your password to confirm");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all data. Enter your password to confirm.")
                .setView(input)
                .setPositiveButton("Delete Account", (dialog, which) -> {
                    String password = input.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String email = user.getEmail();
                    if (email == null) {
                        Toast.makeText(this, "Cannot delete account: no email on file", Toast.LENGTH_LONG).show();
                        return;
                    }
                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                    user.reauthenticate(credential)
                            .addOnSuccessListener(unused -> user.delete()
                                    .addOnSuccessListener(unused2 -> {
                                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()))
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Wrong password. Try again.", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
