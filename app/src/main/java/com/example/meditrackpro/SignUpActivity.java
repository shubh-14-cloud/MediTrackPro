package com.example.meditrackpro;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends BaseActivity {

    private FirebaseAuth auth;
    private String selectedGender = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etDOB = findViewById(R.id.etDOB);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirm = findViewById(R.id.etConfirmPassword);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        Button btnMale = findViewById(R.id.btnMale);
        Button btnFemale = findViewById(R.id.btnFemale);
        Button btnOther = findViewById(R.id.btnOther);

        // Gender selection
        btnMale.setOnClickListener(v -> selectGender(btnMale, "Male", btnFemale, btnOther));
        btnFemale.setOnClickListener(v -> selectGender(btnFemale, "Female", btnMale, btnOther));
        btnOther.setOnClickListener(v -> selectGender(btnOther, "Other", btnMale, btnFemale));

        // Back
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());
        findViewById(R.id.tvGoToSignIn).setOnClickListener(v -> finish());

        // Sign Up
        btnSignUp.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String dob = etDOB.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
            } else if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
            } else if (dob.isEmpty()) {
                Toast.makeText(this, "Enter your date of birth", Toast.LENGTH_SHORT).show();
            } else if (selectedGender.isEmpty()) {
                Toast.makeText(this, "Select your gender", Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Enter a password", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 8) {
                Toast.makeText(this, "Password must be 8+ characters", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                btnSignUp.setEnabled(false);
                btnSignUp.setText("Creating Account...");

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates =
                                        new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                if (user != null) {
                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(t -> {
                                                Toast.makeText(this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(this, MainActivity.class));
                                                finish();
                                            });
                                }
                            } else {
                                btnSignUp.setEnabled(true);
                                btnSignUp.setText("Create Account");
                                String errorMsg;
                                if (task.getException() instanceof FirebaseNetworkException) {
                                    errorMsg = "No internet connection. Please check your network.";
                                } else {
                                    errorMsg = task.getException() != null
                                            ? task.getException().getMessage() : "Sign up failed";
                                }
                                Toast.makeText(this, errorMsg != null ? errorMsg : "Sign up failed",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    private void selectGender(Button selected, String gender, Button... others) {
        selectedGender = gender;
        selected.setBackgroundTintList(ColorStateList.valueOf(Color.argb(80, 255, 255, 255)));
        selected.setTextColor(Color.WHITE);
        for (Button other : others) {
            other.setBackgroundTintList(ColorStateList.valueOf(Color.argb(30, 255, 255, 255)));
            other.setTextColor(Color.argb(160, 255, 255, 255));
        }
    }
}
