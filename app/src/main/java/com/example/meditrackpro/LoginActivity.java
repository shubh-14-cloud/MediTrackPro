package com.example.meditrackpro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.concurrent.Executor;

public class LoginActivity extends BaseActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        // If already logged in
        if (auth.getCurrentUser() != null) {
            SharedPreferences prefs = SecurePrefs.get(this, PrefsConstants.PRIVACY_PREFS);
            if (prefs.getBoolean("biometric", false)) {
                setContentView(R.layout.activity_login);
                applyAccentColor();
                checkBiometric();
                return;
            }
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        applyAccentColor();

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Enter your password", Toast.LENGTH_SHORT).show();
            } else {
                btnSignIn.setEnabled(false);
                btnSignIn.setText("Signing In...");

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                btnSignIn.setEnabled(true);
                                btnSignIn.setText("Sign In");
                                String errorMsg;
                                if (task.getException() instanceof FirebaseNetworkException) {
                                    errorMsg = "No internet connection. Please check your network.";
                                } else {
                                    errorMsg = task.getException() != null
                                            ? task.getException().getMessage() : "Sign in failed";
                                }
                                Toast.makeText(this, errorMsg != null ? errorMsg : "Sign in failed",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // Forgot password
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
            } else {
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Go to Sign Up
        findViewById(R.id.tvGoToSignUp).setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void applyAccentColor() {
        // Sign in button uses glass style from theme; just ensure text is white
        Button btnSignIn = findViewById(R.id.btnSignIn);
        if (btnSignIn != null) {
            btnSignIn.setTextColor(Color.WHITE);
        }

        // Text views stay white
        for (int id : Arrays.asList(R.id.tvForgotPassword, R.id.tvGoToSignUp)) {
            TextView tv = findViewById(id);
            if (tv != null) tv.setTextColor(Color.WHITE);
        }
    }

    private void checkBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(@androidx.annotation.NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(LoginActivity.this, "Verified!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @androidx.annotation.NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(LoginActivity.this, "Use your password to sign in", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(LoginActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("MediTrack Pro")
                .setSubtitle("Verify your identity to continue")
                .setNegativeButtonText("Use Password")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
