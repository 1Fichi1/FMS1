package com.example.fms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Product> productList = new ArrayList<>();
    private ProductAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        recyclerView = findViewById(R.id.catalog_list); // Adjust ID if different
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList, this); // Fixed constructor
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_catalog);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_catalog) {
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                finish();
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

        loadProducts();
    }

    private void loadProducts() {
        db.collection("products")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Ошибка загрузки каталога", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot != null) {
                        productList.clear();
                        for (DocumentSnapshot doc : snapshot) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                productList.add(product);
                            }
                        }
                        adapter.setProducts(productList);
                        if (productList.isEmpty()) {
                            Toast.makeText(this, "Каталог пуст", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}