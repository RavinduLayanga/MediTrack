package com.meditrack.model;

import com.meditrack.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;

public class User extends Person {
    private final int userId;
    private final Role role;
    private final Credentials credentials;
    private final LocalDate dateOfBirth;
    private final LocalDate startingDate;
    private final LocalDateTime addedDate;
    private final LocalDateTime updatedDate;

    // CONSTRUCTOR A: Database Retrieval
    public User(int userId, String fName, String lName, String email, String contactNo,
                Address address, Role role, Credentials credentials, LocalDate dob,
                LocalDate startingDate, LocalDateTime addedDate, LocalDateTime updatedDate) {

        super(fName, lName, email, contactNo, address);

        // 1. REPLACED with your ValidationUtil!
        this.userId = ValidationUtil.validateNonNegativeInt(userId, "User ID");

        this.role = Objects.requireNonNull(role, "Role is required.");
        this.credentials = Objects.requireNonNull(credentials, "Credentials object is required.");
        this.dateOfBirth = validateAge(dob);
        this.startingDate = validateDateNotInFuture(startingDate, "Starting date");
        this.addedDate = addedDate;
        this.updatedDate = updatedDate;
    }

    // CONSTRUCTOR B: New User Creation
    public User(String fName, String lName, String email, String contactNo,
                Address address, Role role, Credentials credentials, LocalDate dob) {

        this(0, fName, lName, email, contactNo, address, role, credentials, dob,
                LocalDate.now(), LocalDateTime.now(), LocalDateTime.now());
    }


    private LocalDate validateAge(LocalDate dob) {
        Objects.requireNonNull(dob, "Date of birth is required.");
        if (dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future.");
        }

        // Check if user is at least 18 years old
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException("User must be at least 18 years old.");
        }
        return dob;
    }

    private LocalDate validateDateNotInFuture(LocalDate date, String fieldName) {
        if (date != null && date.isAfter(LocalDate.now().plusDays(30))) {
            // Allow booking a start date up to 30 days in the future
            throw new IllegalArgumentException(fieldName + " cannot be more than 30 days in the future.");
        }
        return date;
    }

    // Getters
    public int getUserId() { return userId; }
    public Role getRole() { return role; }
    public Credentials getCredentials() { return credentials; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public LocalDate getStartingDate() { return startingDate; }
    public LocalDateTime getAddedDate() { return addedDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }

    @Override
    public String toString() {
        return "User [" +
                "ID=" + userId +
                ", " + super.toString() +
                ", Role=" + role +
                ']';
    }
}