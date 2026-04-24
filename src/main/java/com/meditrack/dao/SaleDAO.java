package com.meditrack.dao;

import com.meditrack.model.Sale;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SaleDAO {
    Sale processSale(Sale sale);
    Sale getSaleById(int id);
    List<Sale> getAllSales();
    List<Sale> getAllSalesByUserId(int userId);
    List<Sale> getAllSalesByDateRange(LocalDate startDate, LocalDate endDate);
}
