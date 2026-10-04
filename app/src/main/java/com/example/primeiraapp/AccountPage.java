package com.example.primeiraapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class AccountPage extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText cellNumber;
    Button btnSave, btnLogout;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);

        mAuth = FirebaseAuth.getInstance();
        cellNumber = findViewById(R.id.phonenumber);
        btnLogout = findViewById(R.id.LogoutButton);
        btnSave = findViewById(R.id.btnSave);
        db = FirebaseFirestore.getInstance();
        loadPhoneNumber();


        btnSave.setOnClickListener(v -> {

                String number = cellNumber.getText().toString().trim();
                if (number.isEmpty()) {
                    Toast.makeText(AccountPage.this, "Por favor, insira o número", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(AccountPage.this, "Erro: Inicia sessão primeiro!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("phoneNumber",number);

                db.collection("Users").document(user.getUid())
                        .set(userUpdates, SetOptions.merge())
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(AccountPage.this, "Guardado na base de dados", Toast.LENGTH_SHORT).show();
                            cellNumber.setText(number);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AccountPage.this, "Erro ao adicionar o número: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
        });
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(AccountPage.this, "Sessão terminada", Toast.LENGTH_SHORT).show();
            Intent goLoginPage = new Intent(AccountPage.this, MainActivity.class);
            startActivity(goLoginPage);
            finish();
        });
    }

    private void loadPhoneNumber() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("Users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("phoneNumber")) {
                            String savedPhone = documentSnapshot.getString("phoneNumber");
                            cellNumber.setText(savedPhone);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AccountPage.this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
