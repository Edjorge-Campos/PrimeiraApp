package com.example.primeiraapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {


    TextView message;
    EditText username, password;
    Button loginButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       message = findViewById(R.id.txtmessage);
       message.setVisibility(GONE);
       username = findViewById(R.id.username);
       password = findViewById(R.id.password);
       loginButton = findViewById(R.id.button);

       loginButton.setOnClickListener(view -> {
           String user = username.getText().toString();
           String pass = password.getText().toString();
           login(user, pass);
        });




    }

    private void login(String user, String pass){
        if(user.equals("admin") && pass.equals("pass")){
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            message.setVisibility(GONE);
        } else {
            message.setVisibility(VISIBLE);
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            message.setText("login failed");
        }
    }


}


