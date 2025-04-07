package com.example.fms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        Context context = holder.itemView.getContext();

        holder.productName.setText(product.getName());
        holder.productDetails.setText(String.format("Цена: %.2f | В наличии: %d",
                product.getPrice(), product.getStock()));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(product.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Проверка состояния "Избранное"
        if (auth.getCurrentUser() != null) {
            String docId = auth.getCurrentUser().getUid() + "_" + product.getName();
            db.collection("favorites").document(docId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                holder.favoriteButton.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_light));
                            } else {
                                holder.favoriteButton.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
                            }
                        }
                    });
        } else {
            holder.favoriteButton.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
        }

        // Обработка нажатия на "Избранное"
        holder.favoriteButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Войдите, чтобы добавить в избранное", Toast.LENGTH_SHORT).show();
                return;
            }

            String docId = auth.getCurrentUser().getUid() + "_" + product.getName();
            db.collection("favorites").document(docId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            // Удаление из избранного
                            db.collection("favorites").document(docId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        holder.favoriteButton.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
                                        Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Добавление в избранное
                            Map<String, Object> favorite = new HashMap<>();
                            favorite.put("userId", auth.getCurrentUser().getUid());
                            favorite.put("productName", product.getName());
                            favorite.put("productPrice", product.getPrice());
                            favorite.put("productStock", product.getStock());
                            favorite.put("productImageUrl", product.getImageUrl());

                            db.collection("favorites").document(docId)
                                    .set(favorite)
                                    .addOnSuccessListener(aVoid -> {
                                        holder.favoriteButton.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_light));
                                        Toast.makeText(context, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
        });

        // Обработка "Добавить в корзину"
        holder.addToCartButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Войдите, чтобы добавить в корзину", Toast.LENGTH_SHORT).show();
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
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Добавлено в корзину", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productDetails;
        ImageButton favoriteButton;
        MaterialButton addToCartButton;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productDetails = itemView.findViewById(R.id.product_details);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_button);
        }
    }
}