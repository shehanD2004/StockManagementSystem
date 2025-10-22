package com.stockmanagementsystem;

import com.stockmanagementsystem.strategy.CsvReportStrategy;
import com.stockmanagementsystem.strategy.ReportContext;
import com.stockmanagementsystem.strategy.TextReportStrategy;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.DocumentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

        // CALCULATE TOTAL VALUE AND CATEGORIES
        double totalValue = 0.0;
        List<StockItem> allItems = stockItemRepository.findAll();

        for (StockItem item : allItems) {
            if (item.getPrice() != null && item.getQuantity() != null) {
                totalValue += item.getPrice() * item.getQuantity();
            }
        }

        long totalCategories = allItems.stream()
                .map(StockItem::getCategory)
                .distinct()
                .count();

        model.addAttribute("totalItems", totalItems);
        model.addAttribute("lowStockItems", lowStockCount);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("totalCategories", totalCategories);

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
                          BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-item-form";
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
            return "edit-item-form";
        }
        item.setId(id);
        stockItemRepository.save(item);
        return "redirect:/items";
    }

    // === DELETE ITEM ===
    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        try {
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
    // Add this autowired field
    @Autowired
    private PdfExportService pdfExportService;

// === PDF EXPORT ENDPOINTS ===

    @GetMapping("/pdf/stock-report")
    public ResponseEntity<byte[]> downloadStockReport() {
        try {
            byte[] pdfBytes = pdfExportService.generateStockReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "stock-report-" + java.time.LocalDate.now() + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (DocumentException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/pdf/low-stock-report")
    public ResponseEntity<byte[]> downloadLowStockReport() {
        try {
            byte[] pdfBytes = pdfExportService.generateLowStockReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "low-stock-alert-" + java.time.LocalDate.now() + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (DocumentException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/pdf/view-stock-report")
    public ResponseEntity<byte[]> viewStockReport() {
        try {
            byte[] pdfBytes = pdfExportService.generateStockReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline",
                    "stock-report-" + java.time.LocalDate.now() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (DocumentException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // === API ENDPOINTS ===
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "API is working!";
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        long totalItems = stockItemRepository.count();
        int lowStockCount = stockItemRepository.findByQuantityLessThan(10).size();

        Double totalValue = stockItemRepository.findAll().stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();

        data.put("totalItems", totalItems);
        data.put("lowStockItems", lowStockCount);
        data.put("totalStockValue", totalValue != null ? totalValue : 0.0);
        data.put("totalSales", 0);
        data.put("totalRevenue", 0.0);
        data.put("period", "Live Database");

        return data;
    }

    @GetMapping("/api/stock-items")
    @ResponseBody
    public List<StockItem> getAllStockItems() {
        return stockItemRepository.findAll();
    }

    @GetMapping("/api/low-stock")
    @ResponseBody
    public List<StockItem> getLowStock() {
        return stockItemRepository.findByQuantityLessThan(10);
    }

    @GetMapping("/debug-charts")
    @ResponseBody
    public String debugCharts() {
        List<Object[]> quantityData = stockItemRepository.findQuantityByCategory();
        List<Object[]> valueData = stockItemRepository.findTotalValueByCategory();

        StringBuilder debug = new StringBuilder();
        debug.append("<h3>Chart Data Debug</h3>");

        debug.append("<h4>Quantity Data:</h4>");
        for (Object[] data : quantityData) {
            debug.append("Category: ").append(data[0])
                    .append(", Total Quantity: ").append(data[1])
                    .append("<br>");
        }

        debug.append("<h4>Value Data:</h4>");
        for (Object[] data : valueData) {
            debug.append("Category: ").append(data[0])
                    .append(", Total Value: ").append(data[1])
                    .append("<br>");
        }

        return debug.toString();
    }
    // Add this autowired field
    @Autowired
    private ReportContext reportContext;

// === STRATEGY PATTERN ENDPOINTS ===

    @GetMapping("/reports")
    public String showReportsPage(Model model) {
        model.addAttribute("currentFormat", reportContext.getCurrentStrategyName());
        return "reports";
    }

    @PostMapping("/reports/set-format")
    public String setReportFormat(@RequestParam String format) {
        switch (format.toUpperCase()) {
            case "TEXT":
                reportContext.setReportStrategy(new TextReportStrategy());
                break;
            case "CSV":
                reportContext.setReportStrategy(new CsvReportStrategy());
                break;
            default:
                // Keep current strategy
                break;
        }
        return "redirect:/reports";
    }

    @GetMapping("/reports/generate")
    public ResponseEntity<byte[]> generateStrategyReport() {
        List<StockItem> items = stockItemRepository.findAll();
        String reportContent = reportContext.generateReport(items);

        String filename = "stock-report-" + java.time.LocalDate.now() +
                getFileExtension(reportContext.getCurrentStrategyName());

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(reportContent.getBytes(StandardCharsets.UTF_8));
    }

    private String getFileExtension(String format) {
        switch (format.toUpperCase()) {
            case "CSV": return ".csv";
            case "TEXT":
            default: return ".txt";
        }
    }
}