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

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView favoritesList;
    private ProductAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        products = new ArrayList<>();

        favoritesList = findViewById(R.id.favorites_list);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        favoritesList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter();
        favoritesList.setAdapter(adapter);

        bottomNavigationView.setSelectedItemId(R.id.nav_favorites);
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
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                finish();
                return true;
            }
            return false;
        });

        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        loadFavorites();
    }

    private void loadFavorites() {
        if (auth.getCurrentUser() != null) {
            db.collection("favorites")
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
            Toast.makeText(this, "Войдите, чтобы увидеть избранное", Toast.LENGTH_SHORT).show();
        }
    }
}