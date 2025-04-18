package com.example.fms;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ImageView imageView = findViewById(R.id.product_image);
        TextView nameText = findViewById(R.id.product_name);
        TextView detailsText = findViewById(R.id.product_details);
        MaterialButton cartButton = findViewById(R.id.add_to_cart_button);

        Product product = (Product) getIntent().getSerializableExtra("product");

        if (product != null) {
            nameText.setText(product.getName());
            detailsText.setText(String.format("Цена: %.2f | В наличии: %d",
                    product.getPrice(), product.getStock()));
            Picasso.get()
                    .load(product.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(imageView);

            cartButton.setOnClickListener(v -> {
                if (auth.getCurrentUser() == null) {
                    Toast.makeText(this, "Войдите, чтобы добавить в корзину", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("userId", auth.getCurrentUser().getUid());
                cartItem.put("productName", product.getName());
                cartItem.put("productPrice", product.getPrice());
                cartItem.put("productStock", product.getStock());
                cartItem.put("productImageUrl", product.getImageUrl());
                cartItem.put("quantity", 1);

                db.collection("cart")
                        .document(auth.getCurrentUser().getUid() + "_" + product.getName())
                        .set(cartItem)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Добавлено в корзину", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
            });
        }
    }
}