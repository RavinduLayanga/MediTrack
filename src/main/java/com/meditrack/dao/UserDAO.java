package com.meditrack.dao;

import com.meditrack.model.User;

import java.util.List;

public interface UserDAO {
    User saveUser(User user, String password);
    User getUserById(int id);
    User getUserbyEmail(String email);
    List<User> getAllUsers();
    User updateUser(User user);
    boolean deleteUser(int id);
}
