package com.stockmanagementsystem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    // Existing method (you probably already have this)
    List<StockItem> findByQuantityLessThan(Integer quantity);

    // NEW: Find items by category
    List<StockItem> findByCategory(String category);

    // NEW: Get total quantity by category for the bar chart
    @Query("SELECT s.category, SUM(s.quantity) FROM StockItem s GROUP BY s.category")
    List<Object[]> findQuantityByCategory();

    // NEW: Get total value by category for the pie chart
    @Query("SELECT s.category, SUM(s.quantity * s.price) FROM StockItem s GROUP BY s.category")
    List<Object[]> findTotalValueByCategory();

}