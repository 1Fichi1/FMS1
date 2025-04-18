package com.example.fms;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        EditText nameInput = findViewById(R.id.product_name_input);
        EditText priceInput = findViewById(R.id.product_price_input);
        EditText stockInput = findViewById(R.id.product_stock_input);
        EditText imageUrlInput = findViewById(R.id.product_image_url_input);
        MaterialButton addButton = findViewById(R.id.add_product_button);

        addButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String priceStr = priceInput.getText().toString();
            String stockStr = stockInput.getText().toString();
            String imageUrl = imageUrlInput.getText().toString();

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                Product product = new Product(name, price, stock, imageUrl);

                db.collection("products").document(name).set(product)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Товар добавлен", Toast.LENGTH_SHORT).show();
                            nameInput.setText("");
                            priceInput.setText("");
                            stockInput.setText("");
                            imageUrlInput.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректные числа", Toast.LENGTH_SHORT).show();
            }
        });
    }
}