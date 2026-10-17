package com.example.meditrackpro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;



import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

/**
 * Shows the patient where they are on a map, and lets them send that location to their caregiver.
 * Background alerts use the same data, so this screen is also the way to check what gets shared.
 */
public class LocationActivity extends BaseActivity implements OnMapReadyCallback {

    private static final float MAP_ZOOM = 16f;

    private GoogleMap map;
    private Marker marker;
    private TextView tvStatus;
    private TextView tvDetail;
    private TextView tvMapNotice;
    private Button btnBackground;
    @Nullable
    private LocationHelper.Snapshot current;

    private final ActivityResultLauncher<String[]> requestLocation =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (LocationHelper.hasForegroundPermission(this)) {
                    refresh();
                } else {
                    tvStatus.setText("Location permission is off");
                    tvDetail.setText("Allow location access to see and share where you are.");
                }
                updateBackgroundButton();
            });

    private final ActivityResultLauncher<String> requestBackground =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> updateBackgroundButton());

    private final ActivityResultLauncher<String> requestSms =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    sendToCaregiver();
                } else {
                    Toast.makeText(this, "SMS permission is required to message your caregiver", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        tvStatus = findViewById(R.id.tvLocationStatus);
        tvDetail = findViewById(R.id.tvLocationDetail);
        tvMapNotice = findViewById(R.id.tvMapNotice);
        btnBackground = findViewById(R.id.btnBackgroundLocation);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefreshLocation).setOnClickListener(v -> refresh());
        findViewById(R.id.btnShareLocation).setOnClickListener(v -> onShareClicked());
        btnBackground.setOnClickListener(v -> askForBackgroundAccess());

        if (BuildConfig.MAPS_KEY_CONFIGURED) {
            SupportMapFragment fragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if (fragment != null) fragment.getMapAsync(this);
        } else {
            showMapNotice("Map unavailable. Add MAPS_API_KEY to local.properties and rebuild.\n\n"
                    + "Your location can still be found and sent to your caregiver.");
        }

        LocationHelper.Snapshot cached = LocationHelper.getCached(this);
        if (cached != null) show(cached);

        if (LocationHelper.hasForegroundPermission(this)) {
            refresh();
        } else {
            requestLocation.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
        updateBackgroundButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBackgroundButton();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        if (night) {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark));
        }
        if (current != null) moveMarker(current);
    }

    private void refresh() {
        tvStatus.setText("Finding your location...");
        LocationHelper.fetchCurrent(this, snapshot -> {
            if (isFinishing()) return;
            if (snapshot == null) {
                tvStatus.setText("Location not available");
                tvDetail.setText("Turn on location services and try again.");
            } else {
                show(snapshot);
            }
        });
    }

    private void show(LocationHelper.Snapshot snapshot) {
        current = snapshot;
        tvStatus.setText(String.format(Locale.getDefault(), "%.5f, %.5f", snapshot.latitude, snapshot.longitude));
        tvDetail.setText(String.format(Locale.getDefault(), "Updated %s · accurate to about %d m",
                LocationHelper.describeAge(snapshot), Math.round(snapshot.accuracyMeters)));
        if (map != null) moveMarker(snapshot);
    }

    private void moveMarker(LocationHelper.Snapshot snapshot) {
        LatLng position = new LatLng(snapshot.latitude, snapshot.longitude);
        if (marker == null) {
            marker = map.addMarker(new MarkerOptions().position(position).title("You are here"));
        } else {
            marker.setPosition(position);
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, MAP_ZOOM));
    }

    private void showMapNotice(String message) {
        tvMapNotice.setText(message);
        tvMapNotice.setVisibility(android.view.View.VISIBLE);
    }

    // ---------------------------------------------------------------- sharing

    private void onShareClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestSms.launch(Manifest.permission.SEND_SMS);
            return;
        }
        sendToCaregiver();
    }

    private void sendToCaregiver() {
        if (current == null) {
            Toast.makeText(this, "No location yet. Wait for the location to appear, then try again.", Toast.LENGTH_LONG).show();
            return;
        }
        String message = "MediTrack: My current location: " + LocationHelper.mapsLink(current)
                + " (" + LocationHelper.describeAge(current) + ")";
        CaregiverSmsSender.send(this, message, true);
    }

    // ---------------------------------------------------------------- background access

    private void updateBackgroundButton() {
        boolean needed = LocationHelper.hasForegroundPermission(this)
                && !LocationHelper.hasBackgroundPermission(this);
        btnBackground.setVisibility(needed ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void askForBackgroundAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ only offers "Allow all the time" from the system settings page.
            Toast.makeText(this, "Open Permissions > Location and choose \"Allow all the time\"", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", getPackageName(), null)));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackground.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
    }
}
