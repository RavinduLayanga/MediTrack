package com.meditrack.dao;

import com.meditrack.model.Supplier;

import java.util.List;

public interface SupplierDAO {
    Supplier saveSupplier(Supplier supplier);
    Supplier getSupplierById(int id);
    Supplier updateSupplier(Supplier supplier);
    List<Supplier> getAllSuppliers();
    boolean deleteSupplier(int id);

}
