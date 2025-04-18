package com.example.fms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private List<Product> productList = new ArrayList<>();
    private ProductAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerView = findViewById(R.id.favorites_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList, this);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
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

        loadFavoriteItems();
    }

    private void loadFavoriteItems() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Войдите, чтобы просмотреть избранное", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("favorites")
                .whereEqualTo("userId", auth.getCurrentUser().getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, "Избранное пусто", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}