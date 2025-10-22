package com.stockmanagementsystem.strategy;

import com.stockmanagementsystem.StockItem;

import java.util.List;

public class TextReportStrategy implements ReportStrategy {
    @Override
    public String generateReport(List<StockItem> items) {
        StringBuilder report = new StringBuilder();
        report.append("=== STOCK INVENTORY REPORT ===\n");
        report.append("Generated on: ").append(java.time.LocalDate.now()).append("\n\n");

        for (StockItem item : items) {
            report.append(String.format("ID: %d | %-15s | %-12s | Qty: %3d | Price: $%6.2f\n",
                    item.getId(), item.getName(), item.getCategory(),
                    item.getQuantity(), item.getPrice()));
        }

        report.append("\nTotal Items: ").append(items.size());
        return report.toString();
    }

    @Override
    public String getFormatName() {
        return "TEXT";
    }
}