package com.example.primeiraapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends AppCompatActivity {

    Button schedules, account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);


       schedules = findViewById(R.id.SchButton);
       account = findViewById(R.id.AccButton);

       schedules.setOnClickListener(view -> {
           Intent goToSchedules  = new Intent(HomePage.this, SchedulePage.class);
           startActivity(goToSchedules);
       });

       account.setOnClickListener(view -> {
           Intent goToAccount = new Intent(HomePage.this, AccountPage.class);
           startActivity(goToAccount);
       });


    }
}

