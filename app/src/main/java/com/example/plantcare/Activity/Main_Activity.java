package com.example.plantcare.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.view.Gravity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.plantcare.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main_Activity extends AppCompatActivity {
    private Classifier mClassifier;
    private Bitmap mBitmap;
    private List<String> diseaseLabels;
    private List<String> medicineNames;

    // Request codes for camera and gallery
    private final int mCameraRequestCode = 0;
    private final int mGalleryRequestCode = 2;

    // Model-related parameters
    private final int mInputSize = 224;  // Model input size (must match model requirements)
    private final String mModelPath = "plant_disease_model.tflite";  // Path to the TFLite model
    private final String mLabelPath = "plant_labels.txt";  // Path to label file
    private final String mSamplePath = "soybean.JPG";  // Path to a sample image for display

    private ImageView mPhotoImageView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main2);

        // Initialize views
        mPhotoImageView = findViewById(R.id.mPhotoImageView);

        // Load disease labels from plant_labels.txt
        loadDiseaseLabels();

        // Load medicine names from medicine_name.txt
        loadMedicineNames();

        // Initialize the classifier
        try {
            mClassifier = new Classifier(getAssets(), mModelPath, mLabelPath, mInputSize);

            // Load and display a sample image from assets
            InputStream inputStream = getResources().getAssets().open(mSamplePath);
            mBitmap = BitmapFactory.decodeStream(inputStream);
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mInputSize, mInputSize, true);
            mPhotoImageView.setImageBitmap(mBitmap);  // Set the sample image to ImageView
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up the Camera button click listener
        findViewById(R.id.mCameraButton).setOnClickListener(v -> {
            Intent callCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(callCameraIntent, mCameraRequestCode);
        });

        // Set up the Gallery button click listener
        findViewById(R.id.mGalleryButton).setOnClickListener(v -> {
            Intent callGalleryIntent = new Intent(Intent.ACTION_PICK);
            callGalleryIntent.setType("image/*");
            startActivityForResult(callGalleryIntent, mGalleryRequestCode);
        });

        // Set up the Detect button click listener
        findViewById(R.id.mDetectButton).setOnClickListener(v -> {
            if (mBitmap != null) {
                // Run model prediction and display the disease name
                Classifier.Recognition results = mClassifier.recognizeImage(mBitmap).get(0);
                String diseaseName = results.getTitle();  // Get the disease name

                // Check if disease name exists in the labels
                if (diseaseLabels.contains(diseaseName)) {
                    // Get the corresponding medicine name
                    String medicineName = getMedicineName(diseaseName);

                    // Show result in a popup
                    showPopup(diseaseName, medicineName);
                } else {
                    Toast.makeText(this, "Disease not found in labels!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No image to detect!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load disease names from plant_labels.txt into a list
    private void loadDiseaseLabels() {
        diseaseLabels = new ArrayList<>();
        try {
            InputStream inputStream = getAssets().open(mLabelPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                diseaseLabels.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load disease labels", Toast.LENGTH_SHORT).show();
        }
    }

    // Load medicine names from medicine_name.txt into a list
    private void loadMedicineNames() {
        medicineNames = new ArrayList<>();
        try {
            InputStream inputStream = getAssets().open("medicine_name.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                medicineNames.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load medicine names", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch medicine name based on the detected disease
    private String getMedicineName(String diseaseName) {
        int index = diseaseLabels.indexOf(diseaseName);
        if (index != -1 && index < medicineNames.size()) {
            return medicineNames.get(index);
        }
        return "No medicine found!";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle Camera Result
        if (requestCode == mCameraRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                mBitmap = (Bitmap) data.getExtras().get("data");
                mBitmap = scaleImage(mBitmap);  // Scale the image to match the model's input size
                Toast.makeText(this, "Image loaded from camera!", Toast.LENGTH_SHORT).show();
                mPhotoImageView.setImageBitmap(mBitmap);  // Set the bitmap to the ImageView
            } else {
                Toast.makeText(this, "Camera action cancelled.", Toast.LENGTH_LONG).show();
            }

            // Handle Gallery Result
        } else if (requestCode == mGalleryRequestCode) {
            if (data != null) {
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    mBitmap = scaleImage(mBitmap);  // Scale the image for prediction
                    mPhotoImageView.setImageBitmap(mBitmap);  // Set the selected image to the ImageView
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_LONG).show();
        }
    }

    // Method to show the popup with the disease and recommended medicine
    private void showPopup(String diseaseName, String medicineName) {
        // Inflate the popup layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.details_popup, null);

        // Create the PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Set the disease name and recommended medicine in the popup
        TextView diseaseTextView = popupView.findViewById(R.id.diseaseName);
        TextView medicineTextView = popupView.findViewById(R.id.medicineName);
        diseaseTextView.setText(diseaseName);
        medicineTextView.setText(medicineName);

        // Set up the "Get It" button click listener
        popupView.findViewById(R.id.getit).setOnClickListener(v -> {
            // Close the popup
            popupWindow.dismiss();

            // Explicitly return to the Main_Activity
            Intent intent = new Intent(this, Main_Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);  // Ensures no new instance of Main_Activity is created
            startActivity(intent);
        });

        // Show the popup in the center of the layout
        ConstraintLayout rootLayout = findViewById(R.id.rootLayout);  // Assuming rootLayout is in your main layout
        popupWindow.showAtLocation(rootLayout, Gravity.CENTER, 0, 0);
    }

    // Helper method to scale the bitmap to match the model's input size
    private Bitmap scaleImage(Bitmap bitmap) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float scaleWidth = ((float) mInputSize) / originalWidth;
        float scaleHeight = ((float) mInputSize) / originalHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, originalWidth, originalHeight, matrix, true);
    }
}
