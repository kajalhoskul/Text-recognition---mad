package com.example.textrecognition;

import static android.Manifest.permission.CAMERA;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {

    private ImageView captureIV;
    private TextView resultTV;
    private Bitmap imgBitmap;

    // Asks the user for the CAMERA permission and reports the answer.
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    this::onCameraPermissionResult);

    // Opens the camera app and returns a preview-sized Bitmap (null if cancelled).
    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(),
                    this::onPictureTaken);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        captureIV = findViewById(R.id.idIVCaptured);
        resultTV = findViewById(R.id.idTVDetectedText);
        Button snapBtn = findViewById(R.id.idBtnSnap);
        Button detectBtn = findViewById(R.id.idBtnDetect);

        detectBtn.setOnClickListener(v -> detectText());
        snapBtn.setOnClickListener(v -> requestCameraAndCapture());
    }

    // ---- Camera permission -------------------------------------------------

    private void requestCameraAndCapture() {
        if (hasCameraPermission()) {
            captureImage();
        } else if (shouldShowRequestPermissionRationale(CAMERA)) {
            // The user said no before: explain why we need it, then ask again.
            new AlertDialog.Builder(this)
                    .setTitle(R.string.camera_permission_title)
                    .setMessage(R.string.camera_permission_rationale)
                    .setPositiveButton(R.string.allow,
                            (d, w) -> cameraPermissionLauncher.launch(CAMERA))
                    .setNegativeButton(R.string.not_now, null)
                    .show();
        } else {
            cameraPermissionLauncher.launch(CAMERA);
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void onCameraPermissionResult(boolean granted) {
        if (granted) {
            captureImage();
        } else if (!shouldShowRequestPermissionRationale(CAMERA)) {
            // Denied with "Don't ask again" (or blocked by policy): the system
            // dialog won't show any more, so the only way back is Settings.
            showOpenSettingsDialog();
        } else {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void showOpenSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.camera_permission_title)
                .setMessage(R.string.camera_permission_blocked)
                .setPositiveButton(R.string.open_settings, (d, w) -> openAppSettings())
                .setNegativeButton(R.string.not_now, null)
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    // ---- Capture -----------------------------------------------------------

    private void captureImage() {
        try {
            takePictureLauncher.launch(null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_camera_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void onPictureTaken(Bitmap bitmap) {
        if (bitmap == null) return; // user backed out of the camera
        imgBitmap = bitmap;
        captureIV.setImageBitmap(imgBitmap);
        resultTV.setText("");
    }

    // ---- Text recognition --------------------------------------------------

    private void detectText() {
        if (imgBitmap == null) {
            Toast.makeText(this, "Snap a picture first", Toast.LENGTH_SHORT).show();
            return;
        }
        InputImage image = InputImage.fromBitmap(imgBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(this::showResult)
                .addOnFailureListener(e -> Toast.makeText(ScannerActivity.this,
                        "Failed to detect text from image: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void showResult(Text text) {
        StringBuilder result = new StringBuilder();
        for (Text.TextBlock block : text.getTextBlocks()) {
            if (result.length() > 0) result.append("\n\n");
            result.append(block.getText());
        }
        if (result.length() == 0) {
            resultTV.setText("No text found");
        } else {
            resultTV.setText(result.toString());
        }
    }
}
