package com.example.meditrackpro;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AddMedicineActivity extends BaseActivity {

    public static final String EXTRA_MEDICINE_ID = "extra_medicine_id";

    private MedicineViewModel viewModel;
    @Nullable
    private Medicine editingMedicine;
    private final List<String> selectedTimes = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        viewModel = new ViewModelProvider(this).get(MedicineViewModel.class);

        EditText etName = findViewById(R.id.etMedicineName);
        EditText etDose = findViewById(R.id.etDose);
        TextView tvTimesDisplay = findViewById(R.id.tvTimesDisplay);
        Button btnAddTime = findViewById(R.id.btnAddTime);
        Button btnClearTimes = findViewById(R.id.btnClearTimes);
        EditText etRefill = findViewById(R.id.etRefillDays);
        Button btnSave = findViewById(R.id.btnSaveMedicine);
        TextView tvTitle = findViewById(R.id.tvBack);

        tvTitle.setOnClickListener(v -> finish());

        btnAddTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(this, R.style.Theme_App_Picker, (picker, hour, minute) -> {
                String formatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                if (!selectedTimes.contains(formatted)) {
                    selectedTimes.add(formatted);
                    Collections.sort(selectedTimes);
                    updateTimesDisplay(tvTimesDisplay);
                } else {
                    Toast.makeText(this, formatted + " already added", Toast.LENGTH_SHORT).show();
                }
            }, 8, 0, true);
            dialog.show();
        });

        btnClearTimes.setOnClickListener(v -> {
            selectedTimes.clear();
            updateTimesDisplay(tvTimesDisplay);
        });

        // Check if we are editing an existing medicine
        int medId = getIntent().getIntExtra(EXTRA_MEDICINE_ID, -1);
        if (medId != -1) {
            btnSave.setText("Update Medication");
            MedicineDatabase.databaseWriteExecutor.execute(() -> {
                Medicine med = MedicineDatabase.getDatabase(AddMedicineActivity.this)
                        .medicineDao().getMedicineById(medId);
                if (med != null) {
                    runOnUiThread(() -> {
                        editingMedicine = med;
                        etName.setText(med.getName());
                        etDose.setText(med.getDose());
                        etRefill.setText(String.valueOf(med.getRefillDays()));
                        // Parse existing comma-separated times back into the list
                        selectedTimes.clear();
                        for (String rawTime : med.getTimes().split(",")) {
                            String trimmed = rawTime.trim();
                            if (!trimmed.isEmpty()) selectedTimes.add(trimmed);
                        }
                        Collections.sort(selectedTimes);
                        updateTimesDisplay(tvTimesDisplay);
                    });
                }
            });
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dose = etDose.getText().toString().trim();
            String refillText = etRefill.getText().toString().trim();
            String times = String.join(", ", selectedTimes);

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter medicine name", Toast.LENGTH_SHORT).show();
            } else if (dose.isEmpty()) {
                Toast.makeText(this, "Enter dose", Toast.LENGTH_SHORT).show();
            } else if (selectedTimes.isEmpty()) {
                Toast.makeText(this, "Add at least one dose time", Toast.LENGTH_SHORT).show();
            } else if (refillText.isEmpty()) {
                Toast.makeText(this, "Enter refill days", Toast.LENGTH_SHORT).show();
            } else {
                Medicine existing = editingMedicine;
                if (existing != null) {
                    viewModel.update(new Medicine(existing.getId(), name, dose, times,
                            Integer.parseInt(refillText), existing.getColor()));
                    Toast.makeText(this, name + " updated!", Toast.LENGTH_SHORT).show();
                } else {
                    viewModel.insert(new Medicine(name, dose, times, Integer.parseInt(refillText)));
                    Toast.makeText(this, name + " saved!", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    private void updateTimesDisplay(TextView tvTimesDisplay) {
        tvTimesDisplay.setText(selectedTimes.isEmpty() ? "" : String.join(", ", selectedTimes));
    }
}
