package com.meditrack.dao;

import com.meditrack.model.Category;
import com.meditrack.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl implements CategoryDAO {

    private static final String SELECT_COLUMNS = "SELECT category_id, category_name ";

    @Override
    public Category saveCategory(Category category) {
        String sql = "INSERT INTO category (category_name) VALUES (?)";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, category.getCategoryName());
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int generatedId = rs.getInt(1);
                            return getCategoryById(generatedId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Category Save Failed] " + e.getMessage());
        }
        return null;
    }

    @Override
    public Category updateCategory(Category category) {
        String sql = "UPDATE category SET category_name = ? WHERE category_id = ?";
        int affectedRows = executeModification(sql, category.getCategoryName(), category.getCategoryId());
        return affectedRows > 0 ? getCategoryById(category.getCategoryId()) : null;
    }

    @Override
    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM category WHERE category_id = ?";
        return executeModification(sql, id) > 0;
    }

    @Override
    public Category getCategoryById(int id) {
        String sql = SELECT_COLUMNS + "FROM category WHERE category_id = ?";
        List<Category> results = fetchCategories(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Category> getAllCategories() {
        String sql = SELECT_COLUMNS + "FROM category ORDER BY category_name ASC";
        return fetchCategories(sql);
    }

    private int executeModification(String sql, Object...params) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                return stmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("[Database Modification Failed] " + e.getMessage());
        }
        return 0;
    }

    private List <Category> fetchCategories(String sql, Object... params) {
        List <Category> categories = new ArrayList();
        try{
            Connection conn = DBConnection.getConnection();
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                for(int i= 0; i< params.length;i++){
                    stmt.setObject(i+1, params[i]);
                }
                try(ResultSet rs = stmt.executeQuery()){
                    while(rs.next()){
                        categories.add(new Category(
                            rs.getInt("category_id"),
                            rs.getString("category_name")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Could not fetch categories:" + e.getMessage());
        }
        return categories;
    }
}
