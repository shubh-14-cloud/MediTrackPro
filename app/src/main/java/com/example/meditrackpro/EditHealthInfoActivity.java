package com.example.meditrackpro;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class EditHealthInfoActivity extends BaseActivity {

    private String selectedGender = "Male";
    private String selectedBloodType = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_health_info);

        SharedPreferences prefs = SecurePrefs.get(this, PrefsConstants.HEALTH_INFO_PREFS);

        EditText etHeight = findViewById(R.id.etHeight);
        EditText etWeight = findViewById(R.id.etWeight);
        TextView tvDOB = findViewById(R.id.tvDOB);
        EditText etConditions = findViewById(R.id.etConditions);
        Button btnMale = findViewById(R.id.btnGenderMale);
        Button btnFemale = findViewById(R.id.btnGenderFemale);
        Button btnOther = findViewById(R.id.btnGenderOther);
        Button btnSave = findViewById(R.id.btnSaveHealthInfo);

        // Blood type buttons
        Map<Integer, String> bloodTypeMap = new LinkedHashMap<>();
        bloodTypeMap.put(R.id.btnBloodOPos, "O+");
        bloodTypeMap.put(R.id.btnBloodONeg, "O-");
        bloodTypeMap.put(R.id.btnBloodAPos, "A+");
        bloodTypeMap.put(R.id.btnBloodANeg, "A-");
        bloodTypeMap.put(R.id.btnBloodBPos, "B+");
        bloodTypeMap.put(R.id.btnBloodBNeg, "B-");
        bloodTypeMap.put(R.id.btnBloodABPos, "AB+");
        bloodTypeMap.put(R.id.btnBloodABNeg, "AB-");

        // Load saved values
        etHeight.setText(prefs.getString("height", ""));
        etWeight.setText(prefs.getString("weight", ""));
        tvDOB.setText(prefs.getString("dob", ""));
        etConditions.setText(prefs.getString("conditions", ""));
        selectedGender = prefs.getString("gender", "Male");
        if (selectedGender == null) selectedGender = "Male";
        selectedBloodType = prefs.getString("blood_type", "");
        if (selectedBloodType == null) selectedBloodType = "";

        updateBloodTypeButtons(bloodTypeMap, selectedBloodType);

        for (Map.Entry<Integer, String> entry : bloodTypeMap.entrySet()) {
            int id = entry.getKey();
            String type = entry.getValue();
            findViewById(id).setOnClickListener(v -> {
                selectedBloodType = type;
                updateBloodTypeButtons(bloodTypeMap, type);
            });
        }

        updateGenderButtons(selectedGender, btnMale, btnFemale, btnOther);
        btnMale.setOnClickListener(v -> {
            selectedGender = "Male";
            updateGenderButtons(selectedGender, btnMale, btnFemale, btnOther);
        });
        btnFemale.setOnClickListener(v -> {
            selectedGender = "Female";
            updateGenderButtons(selectedGender, btnMale, btnFemale, btnOther);
        });
        btnOther.setOnClickListener(v -> {
            selectedGender = "Other";
            updateGenderButtons(selectedGender, btnMale, btnFemale, btnOther);
        });

        // DOB — opens DatePickerDialog on tap
        tvDOB.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dlg = new DatePickerDialog(
                    this,
                    R.style.Theme_App_Picker,
                    (view, year, month, day) -> {
                        String formatted = String.format(
                                Locale.getDefault(), "%d %s %d",
                                day, new DateFormatSymbols().getMonths()[month], year);
                        tvDOB.setText(formatted);
                    },
                    cal.get(Calendar.YEAR) - 20,
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
            dlg.show();
        });

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String height = etHeight.getText().toString().trim();
            String weight = etWeight.getText().toString().trim();

            if (!height.isEmpty()) {
                Float h = parseFloatOrNull(height);
                if (h == null || h < 50f || h > 300f) {
                    Toast.makeText(this, "Please enter a valid height (50–300 cm)", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!weight.isEmpty()) {
                Float w = parseFloatOrNull(weight);
                if (w == null || w < 1f || w > 700f) {
                    Toast.makeText(this, "Please enter a valid weight (1–700 kg)", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String bmi = "";
            if (!height.isEmpty() && !weight.isEmpty()) {
                try {
                    float h = Float.parseFloat(height) / 100f;
                    float w = Float.parseFloat(weight);
                    if (h > 0f) bmi = String.format(Locale.getDefault(), "%.1f", w / (h * h));
                } catch (Exception e) {
                    bmi = "";
                }
            }

            prefs.edit()
                    .putString("blood_type", selectedBloodType)
                    .putString("height", height)
                    .putString("weight", weight)
                    .putString("bmi", bmi)
                    .putString("dob", tvDOB.getText().toString())
                    .putString("gender", selectedGender)
                    .putString("conditions", etConditions.getText().toString().trim())
                    .apply();

            Toast.makeText(this, "Health info saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateBloodTypeButtons(Map<Integer, String> bloodTypeMap, String selected) {
        for (Map.Entry<Integer, String> entry : bloodTypeMap.entrySet()) {
            Button btn = findViewById(entry.getKey());
            UiHelper.setChoiceSelected(btn, entry.getValue().equals(selected));
        }
    }

    private void updateGenderButtons(String selected, Button btnMale, Button btnFemale, Button btnOther) {
        UiHelper.setChoiceSelected(btnMale, "Male".equals(selected));
        UiHelper.setChoiceSelected(btnFemale, "Female".equals(selected));
        UiHelper.setChoiceSelected(btnOther, "Other".equals(selected));
    }

    @Nullable
    private Float parseFloatOrNull(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
