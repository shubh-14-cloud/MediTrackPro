package com.example.meditrackpro;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends BaseActivity {

    private MedicineViewModel sharedMedicineViewModel;

    public MedicineViewModel getSharedMedicineViewModel() {
        return sharedMedicineViewModel;
    }

    private final ActivityResultLauncher<String> requestNotificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "Reminders won't work without notification permission.",
                            Snackbar.LENGTH_LONG
                    ).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedMedicineViewModel = new ViewModelProvider(this).get(MedicineViewModel.class);

        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment =
                (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);
        AppThemeHelper.applyAccentToBottomNav(this, bottomNav);

        requestNotificationPermissionIfNeeded();
        showOnboardingIfNeeded();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void showOnboardingIfNeeded() {
        try {
            SharedPreferences prefs = SecurePrefs.get(this, PrefsConstants.ONBOARDING_PREFS);
            if (!prefs.getBoolean("onboarding_complete", false)) {
                new AlertDialog.Builder(this)
                        .setTitle("Welcome to MediTrackPro!")
                        .setMessage("Add your medications in the Medications tab and set up your profile to get started.")
                        .setPositiveButton("OK", (dialog, which) ->
                                prefs.edit().putBoolean("onboarding_complete", true).apply())
                        .setCancelable(false)
                        .show();
            }
        } catch (Exception e) {
            // Silently ignore if prefs cannot be created (e.g. first run key generation issue)
        }
    }
}
