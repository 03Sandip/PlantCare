package com.example.plantcare.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantcare.Adapter.CatagoryAdapter;
import com.example.plantcare.Domain.CategoryDomain;
import com.example.plantcare.R;

import java.util.ArrayList;

public class MainPage extends AppCompatActivity {
    private RecyclerView recyclerViewCat;
    private CatagoryAdapter catAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page); // Make sure this layout exists

        // Initialize UI elements
        initRecyclerViewCat();
        initLocation();
        initCameraButton();
        initBottomNavBar(); // Adding the bottom navigation functionality here
    }



    private void initLocation() {
        // Initialize spinner for location selection
        String[] items = new String[]{"Durgapur", "Bankura", "Kolkata"};
        Spinner locationSpin = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Correct drop-down resource
        locationSpin.setAdapter(adapter);
    }

    private void initRecyclerViewCat() {
        // Initialize RecyclerView for category list
        ArrayList<CategoryDomain> items = new ArrayList<>();
        items.add(new CategoryDomain("sapling__3_", "Fertilizer"));
        items.add(new CategoryDomain("newspaper", "Newspaper"));
        items.add(new CategoryDomain("cat3", "Vegetable"));
        items.add(new CategoryDomain("cat4", "Vegetable"));
        items.add(new CategoryDomain("cat5", "Vegetable"));
        items.add(new CategoryDomain("cat6", "Vegetable"));

        recyclerViewCat = findViewById(R.id.catview);
        recyclerViewCat.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initialize adapter with the category items
        catAdapter = new CatagoryAdapter(items);
        recyclerViewCat.setAdapter(catAdapter);
    }

    private void initCameraButton() {
        // Initialize camera button and set click listener to start CameraPage
        Button cameraBtn = findViewById(R.id.camera_button);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainPage.this, Main_Activity.class));
            }
        });
    }

    private void initBottomNavBar() {
        // Initialize the Bottom Navigation Bar

        // For "Your Corps" (stay on the same page)
        LinearLayout corpsSection = findViewById(R.id.corpSection); // The LinearLayout containing "Your Corps"
        corpsSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stay on the current page, maybe show a toast message
                Toast.makeText(MainPage.this, "You are already on 'Your Corps' page", Toast.LENGTH_SHORT).show();
            }
        });

        // For "Store" (redirect to StoreActivity)
        LinearLayout storeSection = findViewById(R.id.storeSection); // The LinearLayout containing "Store"
        storeSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, StoreActivity.class);
                startActivity(intent);
            }
        });

        // For "Profile" (redirect to ProfileActivity)
        LinearLayout profileSection = findViewById(R.id.profileSection); // The LinearLayout containing "Profile"
        profileSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}
