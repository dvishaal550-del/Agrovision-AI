package com.agrovision.ai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERM = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 201;
    private static final int REQUEST_IMAGE_PICK = 202;

    private ImageView imagePreview;
    private RadioButton radioDisease;
    private RadioButton radioInsect;
    private TextView txtResult;
    private TextView txtConfidence;
    private TextView txtTreatment;
    private Button btnCamera;
    private Button btnGallery;

    private TFLiteClassifier classifier;
    private Bitmap selectedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagePreview = findViewById(R.id.imagePreview);
        radioDisease = findViewById(R.id.radioDisease);
        radioInsect = findViewById(R.id.radioInsect);
        txtResult = findViewById(R.id.txtResult);
        txtConfidence = findViewById(R.id.txtConfidence);
        txtTreatment = findViewById(R.id.txtTreatment);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        try {
            classifier = new TFLiteClassifier(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize AI models: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERM);
            } else {
                openCamera();
            }
        });

        btnGallery.setOnClickListener(v -> openGallery());

        radioDisease.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && selectedBitmap != null) {
                runInference();
            }
        });

        radioInsect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && selectedBitmap != null) {
                runInference();
            }
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.get("data") != null) {
                    selectedBitmap = (Bitmap) extras.get("data");
                    imagePreview.setImageBitmap(selectedBitmap);
                    runInference();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Uri imageUri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    selectedBitmap = BitmapFactory.decodeStream(inputStream);
                    imagePreview.setImageBitmap(selectedBitmap);
                    runInference();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void runInference() {
        if (selectedBitmap == null || classifier == null) return;

        TFLiteClassifier.PredictionResult result;
        if (radioDisease.isChecked()) {
            result = classifier.predictDisease(selectedBitmap);
        } else {
            result = classifier.predictInsect(selectedBitmap);
        }

        txtResult.setText(result.displayName);
        txtConfidence.setText(String.format("Confidence: %.2f%%", result.confidence * 100.0f));
        txtTreatment.setText(result.treatment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
