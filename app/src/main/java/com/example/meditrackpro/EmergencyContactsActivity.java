package com.example.meditrackpro;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class EmergencyContactsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        Button btnSave = findViewById(R.id.btnSaveContacts);
        btnSave.setTextColor(Color.WHITE);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        SharedPreferences prefs = SecurePrefs.get(this, PrefsConstants.EMERGENCY_PREFS);

        EditText etContact1Name = findViewById(R.id.etContact1Name);
        EditText etContact1Relation = findViewById(R.id.etContact1Relation);
        EditText etContact1Phone = findViewById(R.id.etContact1Phone);
        EditText etContact2Name = findViewById(R.id.etContact2Name);
        EditText etContact2Relation = findViewById(R.id.etContact2Relation);
        EditText etContact2Phone = findViewById(R.id.etContact2Phone);

        // Load saved contacts
        etContact1Name.setText(prefs.getString("c1_name", ""));
        etContact1Relation.setText(prefs.getString("c1_relation", ""));
        etContact1Phone.setText(prefs.getString("c1_phone", ""));
        etContact2Name.setText(prefs.getString("c2_name", ""));
        etContact2Relation.setText(prefs.getString("c2_relation", ""));
        etContact2Phone.setText(prefs.getString("c2_phone", ""));

        btnSave.setOnClickListener(v -> {
            String name1 = etContact1Name.getText().toString().trim();
            String phone1 = etContact1Phone.getText().toString().trim();

            if (name1.isEmpty() || phone1.isEmpty()) {
                Toast.makeText(this, "Please fill in the primary contact", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString("c1_name", name1)
                    .putString("c1_relation", etContact1Relation.getText().toString().trim())
                    .putString("c1_phone", phone1)
                    .putString("c2_name", etContact2Name.getText().toString().trim())
                    .putString("c2_relation", etContact2Relation.getText().toString().trim())
                    .putString("c2_phone", etContact2Phone.getText().toString().trim())
                    .apply();

            Toast.makeText(this, "Contacts saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
