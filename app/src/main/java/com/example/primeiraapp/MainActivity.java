package com.example.primeiraapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    TextView message;
    EditText username, password;
    Button loginButton, registerButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mAuth.getCurrentUser() != null) {
            openHomePage();
            return;
        }

        setContentView(R.layout.activity_main);

       message = findViewById(R.id.message);
       message.setVisibility(GONE);
       username = findViewById(R.id.username);
       password = findViewById(R.id.password);
       loginButton = findViewById(R.id.btnConfirm);
       registerButton = findViewById(R.id.btnRegister);

       loginButton.setOnClickListener(view -> {
           String user = username.getText().toString();
           String pass = password.getText().toString();
           login(user, pass);
        });

       registerButton.setOnClickListener(view ->{
            String newUser = username.getText().toString();
            String newPass = password.getText().toString();
            MakeAccount(newUser, newPass);
        });
    }
    private void login(String user, String pass) {
        mAuth.signInWithEmailAndPassword(user, pass).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                message.setVisibility(VISIBLE);
                message.setText("logged in com sucesso");
                Toast.makeText(this, "logged in com sucesso", Toast.LENGTH_SHORT).show();
                openHomePage();
            } else {
                message.setVisibility(VISIBLE);
                message.setText("Falha ao dar login");
                Toast.makeText(this, "Falha ao dar login", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void MakeAccount(String email, String pass) {
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        message.setVisibility(VISIBLE);
                        message.setText("Registado com sucesso");
                        Toast.makeText(this, "Registado com sucesso", Toast.LENGTH_SHORT).show();
                        Intent goToRegisterMenu = new Intent(MainActivity.this, RegisterMenu.class);
                        startActivity(goToRegisterMenu);
                    } else {
                        message.setVisibility(VISIBLE);
                        message.setText("Falha ao registar: " + task.getException().getMessage());
                        Toast.makeText(this, "Falha ao registar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openHomePage() {
        Intent goToHomePage = new Intent(MainActivity.this, HomePage.class);
        goToHomePage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToHomePage);
    }


}
