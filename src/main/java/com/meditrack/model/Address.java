package com.meditrack.model;

public class Address {
    private String addressLine1;
    private String addressLine2;
    private String city;

    // Constructor
    public Address(String addressLine1, String addressLine2, String city) {
        this.addressLine1 = validateAddressLine(addressLine1, "Address Line 1");
        this.addressLine2 = sanitizeOptionalLine(addressLine2);
        this.city = validateAddressLine(city, "City");
    }

    // Validations
    private String validateAddressLine(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    // Return empty String if the line 2 is null
    private String sanitizeOptionalLine(String value) {
        return (value == null) ? "" : value.trim();
    }

    public String getAddressLine1(){
        return addressLine1;
    }
    public String getAddressLine2(){
        return addressLine2;
    }
    public String getCity(){
        return city;
    }

    @Override
    public String toString(){
        if (addressLine2.isEmpty()){
            return addressLine1+','+city;
        }
        return addressLine1+','+addressLine2+','+city;
    }
}
