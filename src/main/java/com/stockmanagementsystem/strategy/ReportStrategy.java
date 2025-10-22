package com.stockmanagementsystem.strategy;
import com.stockmanagementsystem.StockItem;

import java.util.List;

public interface ReportStrategy {
    String generateReport(List<StockItem> items);
    String getFormatName();
}