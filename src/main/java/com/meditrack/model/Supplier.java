package com.meditrack.model;

import java.util.Objects;
import com.meditrack.util.ValidationUtil;

/**
 * Represents a medical supplier company.
 */
public class Supplier {
    private final int supplierId;
    private final String name;
    private final String contact;
    private final String email;
    private final Address address;

    // Constructor A: For Database Retrieval
    public Supplier(int supplierId, String name, String contact, String email, Address address) {
        this.supplierId = supplierId;
        this.name = ValidationUtil.requireNonBlank(name, "Supplier Name");
        this.contact = ValidationUtil.validateContact(contact);
        this.email = ValidationUtil.validateEmail(email);
        this.address = Objects.requireNonNull(address, "Address is required for a supplier.");
    }

    // Constructor B: For New Supplier Creation
    public Supplier(String name, String contact, String email, Address address) {
        this(0, name, contact, email, address);
    }

    // Getters
    public int getSupplierId() { return supplierId; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getEmail() { return email; }
    public Address getAddress() { return address; }

    @Override
    public String toString() {
        return "Supplier [" +
                "ID=" + supplierId +
                ", Name='" + name + '\'' +
                ", Contact='" + contact + '\'' +
                ']';
    }
}