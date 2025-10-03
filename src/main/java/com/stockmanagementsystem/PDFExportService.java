package com.stockmanagementsystem;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

@Service
public class PDFExportService {

    @Autowired
    private StockItemRepository stockItemRepository;

    public ByteArrayInputStream generateStockReport() {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Stock Inventory Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add date
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph date = new Paragraph("Generated on: " + java.time.LocalDate.now(), dateFont);
            date.setSpacingAfter(20);
            document.add(date);

            // Get all items
            List<StockItem> items = stockItemRepository.findAll();

            // Create table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Table headers
            Stream.of("ID", "Item Name", "Category", "Quantity", "Price", "Total Value")
                    .forEach(headerTitle -> {
                        PdfPCell header = new PdfPCell();
                        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setBorderWidth(2);
                        header.setPhrase(new Phrase(headerTitle, headFont));
                        table.addCell(header);
                    });

            // Table data
            for (StockItem item : items) {
                table.addCell(String.valueOf(item.getId()));
                table.addCell(item.getName());
                table.addCell(item.getCategory());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell("$" + String.format("%.2f", item.getPrice()));
                table.addCell("$" + String.format("%.2f", item.getQuantity() * item.getPrice()));
            }

            document.add(table);

            // Add summary
            long totalItems = stockItemRepository.count();
            int lowStockCount = stockItemRepository.findByQuantityLessThan(10).size();
            double totalValue = items.stream()
                    .mapToDouble(item -> item.getQuantity() * item.getPrice())
                    .sum();

            Paragraph summary = new Paragraph("\n\nSummary:",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            summary.setSpacingAfter(10);
            document.add(summary);

            document.add(new Paragraph("Total Items: " + totalItems));
            document.add(new Paragraph("Low Stock Items: " + lowStockCount));
            document.add(new Paragraph("Total Inventory Value: $" + String.format("%.2f", totalValue)));

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateLowStockReport() {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Low Stock Alert Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add warning
            Font warningFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            warningFont.setColor(BaseColor.RED);
            Paragraph warning = new Paragraph("Items with quantity less than 10", warningFont);
            warning.setSpacingAfter(20);
            document.add(warning);

            // Get low stock items
            List<StockItem> lowStockItems = stockItemRepository.findByQuantityLessThan(10);

            if (lowStockItems.isEmpty()) {
                document.add(new Paragraph("No low stock items found."));
            } else {
                // Create table
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);

                // Table headers
                Stream.of("ID", "Item Name", "Category", "Quantity", "Price")
                        .forEach(headerTitle -> {
                            PdfPCell header = new PdfPCell();
                            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                            header.setBackgroundColor(BaseColor.ORANGE);
                            header.setHorizontalAlignment(Element.ALIGN_CENTER);
                            header.setBorderWidth(2);
                            header.setPhrase(new Phrase(headerTitle, headFont));
                            table.addCell(header);
                        });

                // Table data
                for (StockItem item : lowStockItems) {
                    table.addCell(String.valueOf(item.getId()));
                    table.addCell(item.getName());
                    table.addCell(item.getCategory());

                    // Highlight low quantity in red
                    PdfPCell quantityCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity())));
                    quantityCell.setBackgroundColor(BaseColor.RED);
                    quantityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(quantityCell);

                    table.addCell("$" + String.format("%.2f", item.getPrice()));
                }

                document.add(table);
            }

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}