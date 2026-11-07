package com.example.textrecognition;

import static android.Manifest.permission.CAMERA;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSION_CODE = 200;

    private ImageView captureIV;
    private TextView resultTV;
    private Bitmap imgBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        captureIV = findViewById(R.id.idIVCaptured);
        resultTV = findViewById(R.id.idTVDetectedText);
        Button snapBtn = findViewById(R.id.idBtnSnap);
        Button detectBtn = findViewById(R.id.idBtnDetect);

        detectBtn.setOnClickListener(v -> detectText());
        snapBtn.setOnClickListener(v -> {
            if (checkPermission()) {
                captureImage();
            } else {
                requestPermission();
            }
        });
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }

    private void captureImage() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_CODE) return;
        boolean granted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            Toast.makeText(this, "Permission Granted...", Toast.LENGTH_SHORT).show();
            captureImage();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK
                && data != null && data.getExtras() != null) {
            imgBitmap = (Bitmap) data.getExtras().get("data");
            captureIV.setImageBitmap(imgBitmap);
            resultTV.setText("");
        }
    }

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
