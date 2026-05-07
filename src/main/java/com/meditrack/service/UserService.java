package com.meditrack.service;

import com.meditrack.dao.UserDAO;
import com.meditrack.model.Credentials;
import com.meditrack.model.Role;
import com.meditrack.model.User;

import java.util.List;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String email, String plainPassword) {
        User user = userDAO.getUserbyEmail(email);

        if (user != null) {
            String incomingHash = Credentials.hashPassword(plainPassword);
            String storedHash = user.getCredentials().getHashedPassword();

            if (incomingHash.equals(storedHash)) {
                return user;
            } else {
                System.out.println("[Error] Incorrect password.");
            }
        } else {
            System.out.println("[Error] Email not found.");
        }
        return null;
    }

    public boolean requiresPasswordChange(User user) {
        Credentials creds = user.getCredentials();
        if (creds.getUpdatedAt() == null) {
            return true;
        }
        return creds.getCreatedAt().isEqual(creds.getUpdatedAt());
    }

    public boolean changePassword(User user, String oldPlainPassword, String newPlainPassword) {
        String oldHash = Credentials.hashPassword(oldPlainPassword);
        if (!oldHash.equals(user.getCredentials().getHashedPassword())) {
            System.out.println("[Error] Current password is incorrect. Cannot change password.");
            return false;
        }

        String newHash = Credentials.hashPassword(newPlainPassword);
        boolean success = userDAO.updatePassword(user.getUserId(), newHash);

        if (success) {
            System.out.println("[Success] Password updated successfully.");
        } else {
            System.out.println("[Error] Database failed to update password.");
        }
        return success;
    }


    public User registerUser(User loggedInUser, User newUser, String plainPassword) {
        if (loggedInUser.getRole() != Role.SuperAdmin && loggedInUser.getRole() != Role.Admin) {
            System.out.println("[Error] Unauthorized. Only Administrators can register new users.");
            return null;
        }

        if (userDAO.getUserbyEmail(newUser.getEmail()) != null) {
            System.out.println("[Error] Email already registered. Please use a different email.");
            return null;
        }

        return userDAO.saveUser(newUser, plainPassword);
    }

    public User updateUserProfile(User userToUpdate) {
        User existingUserWithEmail = userDAO.getUserbyEmail(userToUpdate.getEmail());
        if (existingUserWithEmail != null && existingUserWithEmail.getUserId() != userToUpdate.getUserId()) {
            System.out.println("[Error] Update Failed: The email '" + userToUpdate.getEmail() + "' is already in use by another account.");
            return null;
        }

        User updatedUser = userDAO.updateUser(userToUpdate);
        if (updatedUser == null) {
            System.out.println("[Error] Failed to update user profile in the database.");
        } else {
            System.out.println("[Success] User profile updated successfully.");
        }

        return updatedUser;
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(int id) {
        User user = userDAO.getUserById(id);
        if (user == null) {
            System.out.println("[Error] No user found with ID: " + id);
        }
        return user;
    }

    public boolean deleteUser(User loggedInUser, int targetUserId) {
        if (loggedInUser.getRole() != Role.SuperAdmin && loggedInUser.getRole() != Role.Admin) {
            System.out.println("[Error] Unauthorized. Only Administrators can delete users.");
            return false;
        }

        if (loggedInUser.getUserId() == targetUserId) {
            System.out.println("[Error] Safety lock: You cannot delete your own account while logged in.");
            return false;
        }

        boolean success = userDAO.deleteUser(targetUserId);
        if (!success) {
            System.out.println("[Error] Failed to delete user. They may not exist.");
        }
        return success;
    }
}