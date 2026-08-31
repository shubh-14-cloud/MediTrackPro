package com.example.meditrackpro;

import android.graphics.Bitmap;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public final class OcrHelper {

    private OcrHelper() {
    }

    public interface Callback {
        void onSuccess(String text);

        void onError(Exception e);
    }

    /** Runs ML Kit on-device OCR and returns the full extracted text via callback. */
    public static void extractText(Bitmap bitmap, Callback callback) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        recognizer.process(image)
                .addOnSuccessListener(result -> callback.onSuccess(result.getText()))
                .addOnFailureListener(callback::onError);
    }
}
