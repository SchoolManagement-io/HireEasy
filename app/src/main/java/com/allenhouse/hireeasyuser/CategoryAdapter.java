package com.allenhouse.hireeasyuser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<CategoryModel> categoryList;
    private final Context context;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(List<CategoryModel> categoryList, Context context, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_services, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categoryList.get(position);
        holder.textServiceName.setText(category.getCategoryName());
        holder.iconService.setImageResource(category.getIconResId());
        holder.mainBackground.setCardBackgroundColor(category.getBackgroundColor());
        holder.textServiceName.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category.getCategoryName()));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView iconService;
        TextView textServiceName;
        MaterialCardView mainBackground;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            iconService = itemView.findViewById(R.id.iconService);
            textServiceName = itemView.findViewById(R.id.textServiceName);
            mainBackground = itemView.findViewById(R.id.mainBackground);
        }
    }
}