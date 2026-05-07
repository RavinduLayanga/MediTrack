package com.meditrack.dao;

import com.meditrack.model.Address;
import com.meditrack.model.Credentials;
import com.meditrack.model.Role;
import com.meditrack.model.User;
import com.meditrack.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private static final String SELECT_COLUMNS =
            "SELECT u.*, r.role as role_name, c.credentials_id, c.password as hashed_password, " +
                    "c.ipaddress, c.created_at as cred_created, c.updated_at as cred_updated " +
                    "FROM user u " +
                    "JOIN roles r ON u.role_id = r.role_id " +
                    "LEFT JOIN credentials c ON u.user_id = c.user_id ";

    @Override
    public User saveUser(User user, String password) {
        String insertUserSql = "INSERT INTO user (user_fname, user_lname, role_id, email, contact, address_line1, address_line2, city, starting_date, date_of_birth) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertCredSql = "INSERT INTO credentials (user_id, password, ipaddress) VALUES (?, ?, ?)";
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int generatedUserId = 0;

            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, user.getFirstName());
                userStmt.setString(2, user.getLastName());
                userStmt.setInt(3, mapRoleToId(user.getRole()));
                userStmt.setString(4, user.getEmail());
                userStmt.setString(5, user.getContactNo());
                userStmt.setString(6, user.getAddress().getAddressLine1());
                userStmt.setString(7, user.getAddress().getAddressLine2());
                userStmt.setString(8, user.getAddress().getCity());
                userStmt.setDate(9, Date.valueOf(user.getStartingDate()));
                userStmt.setDate(10, Date.valueOf(user.getDateOfBirth()));

                userStmt.executeUpdate();

                try (ResultSet rs = userStmt.getGeneratedKeys()) {
                    if (rs.next()) generatedUserId = rs.getInt(1);
                }
            }

            // Hash Password and Save Credentials using the new User ID
            try (PreparedStatement credStmt = conn.prepareStatement(insertCredSql)) {
                String hashedPw = Credentials.hashPassword(password);

                credStmt.setInt(1, generatedUserId);
                credStmt.setString(2, hashedPw);
                credStmt.setString(3, "127.0.0.1");

                credStmt.executeUpdate();
            }

            conn.commit();
            return getUserById(generatedUserId);

        } catch (SQLException e) {
            System.err.println("[User Save Failed] " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE user SET user_fname = ?, user_lname = ?, role_id = ?, contact = ?, address_line1 = ?, address_line2 = ?, city = ? WHERE user_id = ?";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getFirstName());
                stmt.setString(2, user.getLastName());
                stmt.setInt(3, mapRoleToId(user.getRole()));
                stmt.setString(4, user.getContactNo());
                stmt.setString(5, user.getAddress().getAddressLine1());
                stmt.setString(6, user.getAddress().getAddressLine2());
                stmt.setString(7, user.getAddress().getCity());
                stmt.setInt(8, user.getUserId());

                if (stmt.executeUpdate() > 0) {
                    return getUserById(user.getUserId());
                }
            }
        } catch (SQLException e) {
            System.err.println("[User Update Failed] " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean deleteUser(int id) {
        String deleteCredSql = "DELETE FROM credentials WHERE user_id = ?";
        String deleteUserSql = "DELETE FROM user WHERE user_id = ?";
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement credStmt = conn.prepareStatement(deleteCredSql)) {
                credStmt.setInt(1, id);
                credStmt.executeUpdate();
            }

            try (PreparedStatement userStmt = conn.prepareStatement(deleteUserSql)) {
                userStmt.setInt(1, id);
                userStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("[User Deletion Failed] " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    @Override
    public boolean updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE credentials SET password = ? WHERE user_id = ?";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newHashedPassword);
                stmt.setInt(2, userId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[Password Update Failed] " + e.getMessage());
        }
        return false;
    }

    @Override
    public User getUserById(int id) {
        String sql = SELECT_COLUMNS + "WHERE u.user_id = ?";
        List<User> results = fetchUsers(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public User getUserbyEmail(String email) {
        String sql = SELECT_COLUMNS + "WHERE u.email = ?";
        List<User> results = fetchUsers(sql, email);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<User> getAllUsers() {
        String sql = SELECT_COLUMNS + "ORDER BY u.user_fname ASC";
        return fetchUsers(sql);
    }


    private List<User> fetchUsers(String sql, Object... params) {
        List<User> users = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapRowToUser(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Could not fetch users: " + e.getMessage());
        }
        return users;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        Address address = new Address(
                rs.getString("address_line1"),
                rs.getString("address_line2"),
                rs.getString("city")
        );

        Timestamp credCreated = rs.getTimestamp("cred_created");
        Timestamp credUpdated = rs.getTimestamp("cred_updated");
        Credentials credentials = new Credentials(
                rs.getInt("credentials_id"),
                rs.getInt("user_id"),
                rs.getString("hashed_password"),
                rs.getString("ipaddress"),
                credCreated != null ? credCreated.toLocalDateTime() : null,
                credUpdated != null ? credUpdated.toLocalDateTime() : null
        );

        Role role = Role.valueOf(rs.getString("role_name"));

        Timestamp addedTime = rs.getTimestamp("added_date");
        Timestamp updatedTime = rs.getTimestamp("updated_date");

        return new User(
                rs.getInt("user_id"),
                rs.getString("user_fname"),
                rs.getString("user_lname"),
                rs.getString("email"),
                rs.getString("contact"),
                address,
                role,
                credentials,
                rs.getDate("date_of_birth").toLocalDate(),
                rs.getDate("starting_date").toLocalDate(),
                addedTime != null ? addedTime.toLocalDateTime() : null,
                updatedTime != null ? updatedTime.toLocalDateTime() : null
        );
    }

    private int mapRoleToId(Role role) {
        switch (role) {
            case SuperAdmin: return 1;
            case Admin: return 2;
            case Pharmacist: return 3;
            default: throw new IllegalArgumentException("Unknown Role");
        }
    }
}