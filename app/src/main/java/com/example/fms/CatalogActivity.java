package com.example.fms;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {
    private RecyclerView productList;
    private ProductAdapter adapter;
    private TextInputEditText searchInput;
    private MaterialButton filterButton;
    private FirebaseFirestore db;
    private List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        db = FirebaseFirestore.getInstance();
        products = new ArrayList<>();

        productList = findViewById(R.id.product_list);
        searchInput = findViewById(R.id.search_input);
        filterButton = findViewById(R.id.filter_button);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        productList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter();
        productList.setAdapter(adapter);

        // Иконка профиля
        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Нижняя навигация
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

        // Поиск в реальном времени
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Фильтр и сортировка
        filterButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(CatalogActivity.this, filterButton);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.filter_sort_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(this::onFilterSortItemClick);
            popup.show();
        });

        // Загрузка товаров из Firestore
        loadProducts();
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        products.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            Double price = document.getDouble("price");
                            Long stock = document.getLong("stock");
                            String imageUrl = document.getString("imageUrl"); // URL из Yandex Cloud
                            products.add(new Product(name, price, stock.intValue(), imageUrl));
                        }
                        adapter.setProducts(products);
                    } else {
                        Toast.makeText(this, "Ошибка загрузки: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        adapter.setProducts(filteredList);
    }

    private boolean onFilterSortItemClick(MenuItem item) {
        int itemId = item.getItemId();
        List<Product> filteredList = new ArrayList<>(products);

        if (itemId == R.id.sort_by_price_asc) {
            Collections.sort(filteredList, Comparator.comparingDouble(Product::getPrice));
        } else if (itemId == R.id.sort_by_price_desc) {
            Collections.sort(filteredList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        } else if (itemId == R.id.sort_by_name) {
            Collections.sort(filteredList, Comparator.comparing(Product::getName));
        } else if (itemId == R.id.filter_in_stock) {
            filteredList.removeIf(product -> product.getStock() <= 0);
        }

        adapter.setProducts(filteredList);
        return true;
    }
}