package com.meditrack.model;
import com.meditrack.util.ValidationUtil;

import java.util.Objects;

public abstract class Person {
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String contactNo;
    protected Address address;

    // Constructor
    public Person(String firstName, String lastName, String email, String contactNo, Address address) {
        this.firstName = validateName(firstName, "First Name");
        this.lastName = validateName(lastName, "Last Name");
        this.email = ValidationUtil.validateEmail(email);
        this.contactNo = ValidationUtil.validateContact(contactNo);
        this.address = Objects.requireNonNull(address, "Address cannot be null.");
    }

    // Validation
    private String validateName(String name, String fieldName) {
        String cleanName = ValidationUtil.requireNonBlank(name, fieldName);
        // only letters, allows for a space
        if (!cleanName.matches("^[a-zA-Z\\s]+$")) {
            throw new IllegalArgumentException(fieldName + " must contain only letters.");
        }
        return cleanName.trim();
    }


    public String getFullName() {
        return firstName + " " + lastName;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
    public String getContactNo() {
        return contactNo;
    }

    @Override
    public String toString() {
        return "Name: '" + getFullName() + '\'' +
                ", Contact: '" + contactNo + '\'';
    }

}
