package com.meditrack.dao;
import com.meditrack.model.Category;
import java.util.List;

public interface CategoryDAO {
    Category saveCategory(Category category);
    Category updateCategory(Category category);
    boolean deleteCategory(int id);
    Category getCategoryById(int id);
    List<Category> getAllCategories();
}
