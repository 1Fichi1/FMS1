package com.example.fms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText nameInput, emailInput;
    private MaterialButton saveButton, logoutButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        saveButton = findViewById(R.id.save_button);
        logoutButton = findViewById(R.id.logout_button);

        // Загрузка текущих данных пользователя
        if (mAuth.getCurrentUser() != null) {
            emailInput.setText(mAuth.getCurrentUser().getEmail());
            db.collection("users")
                    .document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            nameInput.setText(document.getString("name"));
                        }
                    });
        }

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", mAuth.getCurrentUser().getEmail());

            db.collection("users")
                    .document(mAuth.getCurrentUser().getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Профиль сохранён", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}