package com.example.meditrackpro;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AddHealthRecordActivity extends BaseActivity {

    private HealthViewModel viewModel;
    private String selectedType = "Lab Result";
    private String photoPath = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_health_record);

        viewModel = new ViewModelProvider(this).get(HealthViewModel.class);

        EditText etTitle = findViewById(R.id.etRecordTitle);
        EditText etDoctor = findViewById(R.id.etDoctorName);
        EditText etNotes = findViewById(R.id.etNotes);
        EditText etStatus = findViewById(R.id.etStatus);
        Button btnSave = findViewById(R.id.btnSaveRecord);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        // Show captured photo if opened from camera
        String extraPhotoPath = getIntent().getStringExtra(CameraActivity.RESULT_PHOTO_PATH);
        photoPath = extraPhotoPath != null ? extraPhotoPath : "";
        if (!photoPath.isEmpty()) {
            ImageView ivPhoto = findViewById(R.id.ivAttachedPhoto);
            if (ivPhoto != null) {
                ivPhoto.setVisibility(View.VISIBLE);
                ivPhoto.setImageBitmap(BitmapFactory.decodeFile(photoPath));
            }
            // Pre-select Prescription type when opened from camera
            selectedType = "Prescription";

            // Auto-fill fields from OCR if available
            String ocrText = getIntent().getStringExtra(CameraActivity.RESULT_OCR_TEXT);
            if (ocrText != null && !ocrText.isEmpty()) {
                String firstLine = "";
                for (String line : ocrText.split("\n")) {
                    String trimmed = line.trim();
                    if (trimmed.length() >= 3) {
                        firstLine = trimmed;
                        break;
                    }
                }
                if (!firstLine.isEmpty() && etTitle.getText().toString().isEmpty()) {
                    etTitle.setText(firstLine);
                }
                etNotes.setText(ocrText);
                Toast.makeText(this, "Text extracted from scan", Toast.LENGTH_SHORT).show();
            }
        }

        Map<Integer, String> typeButtons = new LinkedHashMap<>();
        typeButtons.put(R.id.btnTypeLab, "Lab Result");
        typeButtons.put(R.id.btnTypePrescription, "Prescription");
        typeButtons.put(R.id.btnTypeDoctorVisit, "Doctor Visit");
        typeButtons.put(R.id.btnTypeVaccination, "Vaccination");

        int defaultBtnId = !photoPath.isEmpty() ? R.id.btnTypePrescription : R.id.btnTypeLab;
        updateTypeButtons(typeButtons, defaultBtnId);

        for (Map.Entry<Integer, String> entry : typeButtons.entrySet()) {
            int id = entry.getKey();
            String type = entry.getValue();
            findViewById(id).setOnClickListener(v -> {
                selectedType = type;
                updateTypeButtons(typeButtons, id);
            });
        }

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String doctor = etDoctor.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();
            String status = etStatus.getText().toString().trim();
            if (status.isEmpty()) status = "Added";

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()));
            String fullTitle = !doctor.isEmpty() ? doctor + " - " + title : title;

            viewModel.insert(new HealthRecord(fullTitle, selectedType, today, status, notes, photoPath));
            Toast.makeText(this, "Record saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateTypeButtons(Map<Integer, String> typeButtons, int selectedId) {
        for (int id : typeButtons.keySet()) {
            Button btn = findViewById(id);
            UiHelper.setChoiceSelected(btn, id == selectedId);
        }
    }
}
