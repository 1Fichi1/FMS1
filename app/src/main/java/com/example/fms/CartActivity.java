package com.example.fms;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView cartList;
    private ProductAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        products = new ArrayList<>();

        cartList = findViewById(R.id.cart_list);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        cartList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter();
        cartList.setAdapter(adapter);

        bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_catalog) {
                startActivity(new Intent(this, CatalogActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_cart) {
                return true;
            }
            return false;
        });

        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        loadCart();
    }

    private void loadCart() {
        if (auth.getCurrentUser() != null) {
            db.collection("cart")
                    .whereEqualTo("userId", auth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            products.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("productName");
                                Double price = document.getDouble("productPrice");
                                Long stock = document.getLong("productStock");
                                String imageUrl = document.getString("productImageUrl");
                                products.add(new Product(name, price, stock.intValue(), imageUrl));
                            }
                            adapter.setProducts(products);
                        } else {
                            Toast.makeText(this, "Ошибка загрузки: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Войдите, чтобы увидеть корзину", Toast.LENGTH_SHORT).show();
        }
    }
}