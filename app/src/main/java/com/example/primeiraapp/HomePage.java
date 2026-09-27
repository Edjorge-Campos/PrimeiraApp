package com.example.primeiraapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {

    Button schedules, account, availability;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);


       schedules = findViewById(R.id.SchButton);
       account = findViewById(R.id.AccButton);
       availability = findViewById(R.id.dispButton);

       schedules.setOnClickListener(view -> {
           Intent goToSchedules  = new Intent(HomePage.this, SchedulePage.class);
           startActivity(goToSchedules);
       });

       account.setOnClickListener(view -> {
           Intent goToAccount = new Intent(HomePage.this, AccountPage.class);
           startActivity(goToAccount);
       });

       availability.setOnClickListener(view ->{
           Intent goToDisponibility = new Intent(HomePage.this, Disponibility.class);
           startActivity(goToDisponibility);
        });


    }
}
