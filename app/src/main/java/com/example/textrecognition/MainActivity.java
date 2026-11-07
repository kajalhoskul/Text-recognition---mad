package com.example.textrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button captureBtn = findViewById(R.id.idBtnCapture);
        captureBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ScannerActivity.class)));
    }
}
