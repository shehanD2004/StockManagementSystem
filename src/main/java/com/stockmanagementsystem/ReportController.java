package com.stockmanagementsystem;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ReportController {

    private final StockItemRepository stockItemRepository;

    @Autowired
    public ReportController(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    // === DASHBOARD ===
    @GetMapping("/")
    public String dashboard(Model model) {
        long totalItems = stockItemRepository.count();
        int lowStockCount = stockItemRepository.findByQuantityLessThan(10).size();
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("lowStockItems", lowStockCount);
        return "dashboard";
    }

    // === VIEW ALL ITEMS ===
    @GetMapping("/items")
    public String showItems(Model model) {
        List<StockItem> items = stockItemRepository.findAll();
        model.addAttribute("items", items);
        return "items-list";
    }

    // === ADD NEW ITEM ===
    @GetMapping("/add-item")
    public String showAddForm(Model model) {
        model.addAttribute("stockItem", new StockItem());
        return "add-item-form";
    }

    @PostMapping("/add-item")
    public String addItem(@Valid @ModelAttribute StockItem stockItem,
                          BindingResult result, Model model) {  // ← ADD @Valid and BindingResult
        if (result.hasErrors()) {
            return "add-item-form"; // Return to form with error messages
        }
        stockItemRepository.save(stockItem);
        return "redirect:/items";
    }

    // === EDIT ITEM ===
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        StockItem item = stockItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item Id:" + id));
        model.addAttribute("stockItem", item);
        return "edit-item-form";
    }

    @PostMapping("/update/{id}")
    public String updateItem(@PathVariable Long id,
                             @Valid @ModelAttribute StockItem item,
                             BindingResult result) {
        if (result.hasErrors()) {
            return "edit-item-form"; // Return to form with error messages
        }
        item.setId(id);
        stockItemRepository.save(item);
        return "redirect:/items";
    }
    // === DELETE ITEM ===
    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        try {
            // Check if item exists before deleting
            if (stockItemRepository.existsById(id)) {
                stockItemRepository.deleteById(id);
            } else {
                System.out.println("Item with ID " + id + " not found");
            }
        } catch (Exception e) {
            System.out.println("Error deleting item: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/items";
    }

    // === LOW STOCK ALERTS ===
    @GetMapping("/low-stock")
    public String showLowStock(Model model) {
        List<StockItem> lowStockItems = stockItemRepository.findByQuantityLessThan(10);
        model.addAttribute("lowStockItems", lowStockItems);
        return "low-stock-alerts";
    }

    // === CHARTS ===
    @GetMapping("/charts")
    public String showCharts(Model model) {
        List<Object[]> quantityData = stockItemRepository.findQuantityByCategory();
        List<Object[]> valueData = stockItemRepository.findTotalValueByCategory();

        model.addAttribute("quantityData", quantityData);
        model.addAttribute("valueData", valueData);
        return "charts";
    }

    // === ADD THESE API ENDPOINTS RIGHT HERE ===

    // Test endpoint
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "API is working!";
    }

    // Dashboard data API
    @GetMapping("/api/dashboard")
    @ResponseBody
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        long totalItems = stockItemRepository.count();
        int lowStockCount = stockItemRepository.findByQuantityLessThan(10).size();

        // Calculate total stock value
        Double totalValue = stockItemRepository.findAll().stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();

        data.put("totalItems", totalItems);
        data.put("lowStockItems", lowStockCount);
        data.put("totalStockValue", totalValue != null ? totalValue : 0.0);
        data.put("totalSales", 0); // You'll need to implement this
        data.put("totalRevenue", 0.0); // You'll need to implement this
        data.put("period", "Live Database");

        return data;
    }

    // All stock items API
    @GetMapping("/api/stock-items")
    @ResponseBody
    public List<StockItem> getAllStockItems() {
        return stockItemRepository.findAll();
    }

    // Low stock API
    @GetMapping("/api/low-stock")
    @ResponseBody
    public List<StockItem> getLowStock() {
        return stockItemRepository.findByQuantityLessThan(10);
    }

}