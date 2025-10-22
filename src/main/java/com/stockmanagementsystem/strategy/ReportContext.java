package com.stockmanagementsystem.strategy;

import com.stockmanagementsystem.StockItem;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ReportContext {
    private ReportStrategy currentStrategy;

    public ReportContext() {
        // Default strategy
        this.currentStrategy = new TextReportStrategy();
    }

    public void setReportStrategy(ReportStrategy strategy) {
        this.currentStrategy = strategy;
    }

    public String generateReport(List<StockItem> items) {
        return currentStrategy.generateReport(items);
    }

    public String getCurrentStrategyName() {
        return currentStrategy.getFormatName();
    }
}