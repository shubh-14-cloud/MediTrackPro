package com.example.meditrackpro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CameraActivity extends BaseActivity {

    public static final String RESULT_PHOTO_PATH = "result_photo_path";
    public static final String RESULT_OCR_TEXT = "result_ocr_text";

    /** Number of days after which captured prescription images are deleted */
    private static final long CLEANUP_DAYS_THRESHOLD = 30L;

    /** Milliseconds in a single day, used for cutoff calculation */
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

    private ImageCapture imageCapture;
    @Nullable
    private String capturedFilePath;

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to scan documents", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        MedicineDatabase.databaseWriteExecutor.execute(this::cleanupOldImages);

        findViewById(R.id.tvCameraBack).setOnClickListener(v -> finish());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }

        findViewById(R.id.btnRetake).setOnClickListener(v -> {
            findViewById(R.id.layoutPreview).setVisibility(View.GONE);
            capturedFilePath = null;
        });

        Button btnUsePhoto = findViewById(R.id.btnUsePhoto);
        btnUsePhoto.setOnClickListener(v -> {
            String path = capturedFilePath;
            if (path == null) return;
            btnUsePhoto.setEnabled(false);
            Toast.makeText(this, "Scanning text...", Toast.LENGTH_SHORT).show();

            MedicineDatabase.databaseWriteExecutor.execute(() -> {
                android.graphics.Bitmap bitmap = BitmapFactory.decodeFile(path);
                runOnUiThread(() -> OcrHelper.extractText(bitmap, new OcrHelper.Callback() {
                    @Override
                    public void onSuccess(String text) {
                        launchAddRecord(path, text);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("CameraActivity", "OCR failed", e);
                        launchAddRecord(path, "");
                    }
                }));
            });
        });
    }

    private void launchAddRecord(String path, String ocrText) {
        Intent intent = new Intent(CameraActivity.this, AddHealthRecordActivity.class);
        intent.putExtra(RESULT_PHOTO_PATH, path);
        intent.putExtra(RESULT_OCR_TEXT, ocrText);
        startActivity(intent);
        finish();
    }

    private void startCamera() {
        PreviewView previewView = findViewById(R.id.cameraPreview);
        View btnCapture = findViewById(R.id.btnCapture);
        View layoutPreview = findViewById(R.id.layoutPreview);
        ImageView ivCaptured = findViewById(R.id.ivCaptured);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (Exception e) {
                Log.e("CameraActivity", "Camera bind failed", e);
                Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));

        btnCapture.setOnClickListener(v -> {
            ImageCapture capture = imageCapture;
            if (capture == null) return;

            File outDir = new File(getFilesDir(), "prescriptions");
            outDir.mkdirs();
            String fileName = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.getDefault())) + ".jpg";
            File outFile = new File(outDir, fileName);

            capture.takePicture(
                    new ImageCapture.OutputFileOptions.Builder(outFile).build(),
                    ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                            capturedFilePath = outFile.getAbsolutePath();
                            ivCaptured.setImageBitmap(BitmapFactory.decodeFile(outFile.getAbsolutePath()));
                            layoutPreview.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exc) {
                            Toast.makeText(CameraActivity.this, "Capture failed: " + exc.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });
    }

    private void cleanupOldImages() {
        File outDir = new File(getFilesDir(), "prescriptions");
        if (!outDir.exists()) return;
        long cutoff = System.currentTimeMillis() - CLEANUP_DAYS_THRESHOLD * MILLIS_PER_DAY;
        File[] files = outDir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isFile() && file.lastModified() < cutoff) {
                file.delete();
            }
        }
    }
}
