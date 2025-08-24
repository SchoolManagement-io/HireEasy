package com.allenhouse.hireeasyuser;

public class CategoryModel {
    private String categoryName;
    private int iconResId;
    private int backgroundColor;

    public CategoryModel(String categoryName, int iconResId, int backgroundColor) {
        this.categoryName = categoryName;
        this.iconResId = iconResId;
        this.backgroundColor = backgroundColor;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}