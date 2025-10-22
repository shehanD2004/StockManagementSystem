package com.stockmanagementsystem.strategy;

import com.stockmanagementsystem.StockItem;

import java.util.List;

public class CsvReportStrategy implements ReportStrategy {
    @Override
    public String generateReport(List<StockItem> items) {
        StringBuilder report = new StringBuilder();
        report.append("ID,Name,Category,Quantity,Price\n");

        for (StockItem item : items) {
            report.append(String.format("%d,%s,%s,%d,%.2f\n",
                    item.getId(), item.getName(), item.getCategory(),
                    item.getQuantity(), item.getPrice()));
        }

        return report.toString();
    }

    @Override
    public String getFormatName() {
        return "CSV";
    }
}