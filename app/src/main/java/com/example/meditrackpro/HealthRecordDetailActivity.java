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

public class HealthRecordDetailActivity extends BaseActivity {

    public static final String EXTRA_RECORD_ID = "extra_record_id";

    private HealthViewModel viewModel;
    @Nullable
    private HealthRecord currentRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_record_detail);

        viewModel = new ViewModelProvider(this).get(HealthViewModel.class);

        TextView tvBack = findViewById(R.id.tvDetailBack);
        TextView tvIcon = findViewById(R.id.tvDetailIcon);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvType = findViewById(R.id.tvDetailType);
        TextView tvStatus = findViewById(R.id.tvDetailStatus);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvNotes = findViewById(R.id.tvDetailNotes);
        ImageView ivPhoto = findViewById(R.id.ivDetailPhoto);
        EditText etStatus = findViewById(R.id.etDetailStatus);
        EditText etNotes = findViewById(R.id.etDetailNotes);
        Button btnSave = findViewById(R.id.btnDetailSave);
        Button btnDelete = findViewById(R.id.btnDetailDelete);

        tvBack.setOnClickListener(v -> finish());

        int recordId = getIntent().getIntExtra(EXTRA_RECORD_ID, -1);
        if (recordId == -1) {
            finish();
            return;
        }

        // Observe to get the record
        viewModel.getAllRecords().observe(this, records -> {
            HealthRecord found = null;
            for (HealthRecord r : records) {
                if (r.getId() == recordId) {
                    found = r;
                    break;
                }
            }
            if (found != null && currentRecord == null) {
                currentRecord = found;
                bindRecord(found, tvIcon, tvTitle, tvType, tvStatus, tvDate, tvNotes, ivPhoto, etStatus, etNotes);
            }
        });

        btnSave.setOnClickListener(v -> {
            HealthRecord record = currentRecord;
            if (record == null) return;
            String newStatus = etStatus.getText().toString().trim();
            if (newStatus.isEmpty()) newStatus = record.getStatus();
            HealthRecord updated = new HealthRecord(record.getId(), record.getTitle(), record.getType(),
                    record.getDate(), newStatus, etNotes.getText().toString().trim(), record.getPhotoPath());
            viewModel.update(updated);
            Toast.makeText(this, "Record updated", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            HealthRecord record = currentRecord;
            if (record == null) return;
            viewModel.delete(record);
            Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void bindRecord(HealthRecord record, TextView tvIcon, TextView tvTitle, TextView tvType,
                             TextView tvStatus, TextView tvDate, TextView tvNotes, ImageView ivPhoto,
                             EditText etStatus, EditText etNotes) {
        tvTitle.setText(record.getTitle());
        tvType.setText(record.getType());
        tvDate.setText(record.getDate());
        tvNotes.setText(record.getNotes().isEmpty() ? "No notes" : record.getNotes());
        tvStatus.setText(record.getStatus());

        tvIcon.setText(HealthRecordAdapter.typeCode(record.getType()));

        etStatus.setText(record.getStatus());
        etNotes.setText(record.getNotes());

        if (!record.getPhotoPath().isEmpty()) {
            try {
                android.graphics.Bitmap bmp = BitmapFactory.decodeFile(record.getPhotoPath());
                if (bmp != null) {
                    ivPhoto.setImageBitmap(bmp);
                    ivPhoto.setVisibility(View.VISIBLE);
                } else {
                    ivPhoto.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                ivPhoto.setVisibility(View.GONE);
            }
        }
    }
}
