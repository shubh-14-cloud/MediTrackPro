package com.example.meditrackpro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        TextView tvProfileName = view.findViewById(R.id.tvProfileName);

        // Firebase display name
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            tvProfileName.setText(user.getDisplayName());
        }

        // Edit name
        view.findViewById(R.id.tvEditName).setOnClickListener(v -> {
            String currentName = auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null
                    ? auth.getCurrentUser().getDisplayName() : "";
            EditText input = new EditText(requireContext());
            input.setText(currentName);
            input.setHint("Your name");
            input.setPadding(48, 32, 48, 32);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Edit Name")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newName = input.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FirebaseUser current = auth.getCurrentUser();
                        if (current == null) return;
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build();
                        current.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                tvProfileName.setText(newName);
                                Toast.makeText(requireContext(), "Name updated!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to update name. Check your connection.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Sign out
        view.findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        // Edit health info
        view.findViewById(R.id.tvEditHealthInfo).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditHealthInfoActivity.class)));

        // Caregiver card
        view.findViewById(R.id.cardCaregiver).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CaregiverSettingsActivity.class)));

        // Settings rows
        view.findViewById(R.id.settingNotifications).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationSettingsActivity.class)));
        view.findViewById(R.id.settingLocation).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), LocationActivity.class)));
        view.findViewById(R.id.settingPrivacy).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PrivacySettingsActivity.class)));
        view.findViewById(R.id.settingRemedies).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RemediesActivity.class)));
        view.findViewById(R.id.settingEmergency).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EmergencyContactsActivity.class)));
        view.findViewById(R.id.settingAppearance).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AppAppearanceActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null) return;

        SharedPreferences prefs = SecurePrefs.get(requireContext(), PrefsConstants.HEALTH_INFO_PREFS);

        // Health info values
        String bloodType = prefs.getString("blood_type", "");
        ((TextView) view.findViewById(R.id.tvBloodTypeValue)).setText(
                bloodType != null && !bloodType.isEmpty() ? bloodType : "--");

        String height = prefs.getString("height", "");
        ((TextView) view.findViewById(R.id.tvHeightValue)).setText(
                height != null && !height.isEmpty() ? height + " cm" : "--");

        String weight = prefs.getString("weight", "");
        ((TextView) view.findViewById(R.id.tvWeightValue)).setText(
                weight != null && !weight.isEmpty() ? weight + " kg" : "--");

        String bmi = prefs.getString("bmi", "");
        ((TextView) view.findViewById(R.id.tvBMIValue)).setText(
                bmi != null && !bmi.isEmpty() ? bmi : "--");

        // DOB + Gender in profile card
        String dob = prefs.getString("dob", "");
        if (dob == null) dob = "";
        String gender = prefs.getString("gender", "");
        if (gender == null) gender = "";
        List<String> parts = new ArrayList<>();
        if (!dob.isEmpty()) parts.add(dob);
        if (!gender.isEmpty()) parts.add(gender);
        String dobGenderText = String.join(" · ", parts);
        ((TextView) view.findViewById(R.id.tvProfileDOBGender)).setText(
                !dobGenderText.isEmpty() ? "DOB: " + dobGenderText : "");

        // Conditions as dynamic badges
        LinearLayout conditionsLayout = view.findViewById(R.id.layoutConditionBadges);
        conditionsLayout.removeAllViews();
        String conditions = prefs.getString("conditions", "");
        if (conditions == null) conditions = "";
        if (!conditions.isEmpty()) {
            String[] conditionArray = conditions.split(",");
            for (String condition : conditionArray) {
                String trimmed = condition.trim();
                if (!trimmed.isEmpty()) {
                    TextView badge = new TextView(requireContext());
                    badge.setText(trimmed);
                    badge.setTextSize(12f);
                    badge.setTypeface(null, Typeface.BOLD);
                    badge.setBackgroundResource(R.drawable.bg_badge);
                    badge.setTextColor(UiHelper.color(requireContext(), R.color.accent));
                    int h = UiHelper.dp(requireContext(), 10);
                    int v = UiHelper.dp(requireContext(), 4);
                    badge.setPadding(h, v, h, v);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMarginEnd(UiHelper.dp(requireContext(), 8));
                    badge.setLayoutParams(params);
                    conditionsLayout.addView(badge);
                }
            }
        }

        // Caregiver
        SharedPreferences caregiverPrefs = SecurePrefs.get(requireContext(), PrefsConstants.CAREGIVER_PREFS);
        String caregiverName = caregiverPrefs.getString("name", "");
        if (caregiverName == null) caregiverName = "";
        String caregiverRelation = caregiverPrefs.getString("relation", "");
        if (caregiverRelation == null) caregiverRelation = "";
        boolean isActive = caregiverPrefs.getBoolean("active", false);

        if (!caregiverName.isEmpty()) {
            ((TextView) view.findViewById(R.id.tvCaregiverName)).setText(caregiverName);
            ((TextView) view.findViewById(R.id.tvCaregiverRelation)).setText(
                    !caregiverRelation.isEmpty() ? caregiverRelation + " · Gets missed-dose alerts"
                            : "Gets missed-dose alerts");
            ((TextView) view.findViewById(R.id.tvCaregiverStatus)).setText(isActive ? "Active" : "›");
        } else {
            ((TextView) view.findViewById(R.id.tvCaregiverName)).setText("Not set");
            ((TextView) view.findViewById(R.id.tvCaregiverRelation)).setText("Tap to add a caregiver");
            ((TextView) view.findViewById(R.id.tvCaregiverStatus)).setText("›");
        }
    }
}
