package com.stockmanagementsystem;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfExportService {

    @Autowired
    private StockItemRepository stockItemRepository;

    public byte[] generateStockReport() throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("STOCK INVENTORY REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add Date
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
            Paragraph date = new Paragraph("Generated on: " + java.time.LocalDate.now(), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
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
            String[] headers = {"ID", "Item Name", "Category", "Quantity", "Price", "Total Value"};
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell();
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setBorderWidth(1);
                headerCell.setPhrase(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(5);
                table.addCell(headerCell);
            }

            // Table data
            double totalInventoryValue = 0;
            for (StockItem item : items) {
                // ID
                table.addCell(createTableCell(String.valueOf(item.getId()), Element.ALIGN_CENTER));

                // Name
                table.addCell(createTableCell(item.getName(), Element.ALIGN_LEFT));

                // Category
                table.addCell(createTableCell(item.getCategory(), Element.ALIGN_LEFT));

                // Quantity (highlight low stock)
                PdfPCell quantityCell = createTableCell(String.valueOf(item.getQuantity()), Element.ALIGN_CENTER);
                if (item.getQuantity() < 10) {
                    quantityCell.setBackgroundColor(BaseColor.ORANGE);
                }
                table.addCell(quantityCell);

                // Price
                table.addCell(createTableCell("$" + String.format("%.2f", item.getPrice()), Element.ALIGN_RIGHT));

                // Total Value
                double itemValue = item.getQuantity() * item.getPrice();
                totalInventoryValue += itemValue;
                table.addCell(createTableCell("$" + String.format("%.2f", itemValue), Element.ALIGN_RIGHT));
            }

            document.add(table);

            // Add summary
            Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
            Paragraph summary = new Paragraph("\nSUMMARY", summaryFont);
            summary.setSpacingAfter(10);
            document.add(summary);

            // Summary details
            long totalItems = stockItemRepository.count();
            int lowStockCount = stockItemRepository.findByQuantityLessThan(10).size();
            long categoryCount = stockItemRepository.findAll().stream()
                    .map(StockItem::getCategory)
                    .distinct()
                    .count();

            document.add(new Paragraph("Total Items: " + totalItems));
            document.add(new Paragraph("Low Stock Items (Qty < 10): " + lowStockCount));
            document.add(new Paragraph("Categories: " + categoryCount));

            Font totalValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLUE);
            Paragraph totalValue = new Paragraph("Total Inventory Value: $" + String.format("%.2f", totalInventoryValue), totalValueFont);
            totalValue.setSpacingBefore(10);
            document.add(totalValue);

            // Add footer
            Paragraph footer = new Paragraph("\n\n--- End of Report ---",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (DocumentException e) {
            throw e;
        }

        return out.toByteArray();
    }

    public byte[] generateLowStockReport() throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add Title with warning color
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.RED);
            Paragraph title = new Paragraph("LOW STOCK ALERT REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // Warning message
            Font warningFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.ORANGE);
            Paragraph warning = new Paragraph("Items with quantity less than 10", warningFont);
            warning.setAlignment(Element.ALIGN_CENTER);
            warning.setSpacingAfter(20);
            document.add(warning);

            // Get low stock items
            List<StockItem> lowStockItems = stockItemRepository.findByQuantityLessThan(10);

            if (lowStockItems.isEmpty()) {
                Paragraph noItems = new Paragraph("No low stock items found. All items have sufficient stock.",
                        FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GREEN));
                noItems.setAlignment(Element.ALIGN_CENTER);
                document.add(noItems);
            } else {
                // Create table
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);

                // Table headers
                String[] headers = {"ID", "Item Name", "Category", "Quantity", "Price"};
                for (String header : headers) {
                    PdfPCell headerCell = new PdfPCell();
                    headerCell.setBackgroundColor(BaseColor.ORANGE);
                    headerCell.setBorderWidth(1);
                    headerCell.setPhrase(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                    headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    headerCell.setPadding(5);
                    table.addCell(headerCell);
                }

                // Table data
                for (StockItem item : lowStockItems) {
                    table.addCell(createTableCell(String.valueOf(item.getId()), Element.ALIGN_CENTER));
                    table.addCell(createTableCell(item.getName(), Element.ALIGN_LEFT));
                    table.addCell(createTableCell(item.getCategory(), Element.ALIGN_LEFT));

                    // Highlight critical low stock
                    PdfPCell quantityCell = createTableCell(String.valueOf(item.getQuantity()), Element.ALIGN_CENTER);
                    if (item.getQuantity() < 5) {
                        quantityCell.setBackgroundColor(BaseColor.RED);
                        quantityCell.setPhrase(new Phrase(String.valueOf(item.getQuantity()),
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE)));
                    } else {
                        quantityCell.setBackgroundColor(BaseColor.ORANGE);
                    }
                    table.addCell(quantityCell);

                    table.addCell(createTableCell("$" + String.format("%.2f", item.getPrice()), Element.ALIGN_RIGHT));
                }

                document.add(table);

                // Urgent action required
                if (lowStockItems.stream().anyMatch(item -> item.getQuantity() < 3)) {
                    Paragraph urgent = new Paragraph("\nURGENT: Some items have critically low stock (less than 3)!",
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.RED));
                    urgent.setSpacingBefore(15);
                    document.add(urgent);
                }
            }

            document.close();

        } catch (DocumentException e) {
            throw e;
        }

        return out.toByteArray();
    }

    private PdfPCell createTableCell(String content, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content,
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setBorderWidth(1);
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }
}