package com.college.booking.service;

import com.college.booking.entity.Booking;
import com.college.booking.repository.BookingRepository;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final BookingRepository bookingRepository;
    private final DashboardService dashboardService;

    public ReportService(BookingRepository bookingRepository, DashboardService dashboardService) {
        this.bookingRepository = bookingRepository;
        this.dashboardService = dashboardService;
    }

    public byte[] csv(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingRepository.findBetween(from, to);
        StringBuilder sb = new StringBuilder("id,date,start,end,status,title,user,kind\n");
        for (Booking b : bookings) {
            sb.append(b.getId()).append(',')
                    .append(b.getBookingDate()).append(',')
                    .append(b.getStartTime()).append(',')
                    .append(b.getEndTime()).append(',')
                    .append(b.getStatus()).append(',')
                    .append(esc(b.getTitle())).append(',')
                    .append(esc(b.getUser().getEmail())).append(',')
                    .append(b.getBookingKind()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] excel(LocalDate from, LocalDate to) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Bookings");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Date", "Start", "End", "Status", "Title", "User", "Kind"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int r = 1;
            for (Booking b : bookingRepository.findBetween(from, to)) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(b.getId());
                row.createCell(1).setCellValue(b.getBookingDate().toString());
                row.createCell(2).setCellValue(b.getStartTime().toString());
                row.createCell(3).setCellValue(b.getEndTime().toString());
                row.createCell(4).setCellValue(b.getStatus().name());
                row.createCell(5).setCellValue(b.getTitle());
                row.createCell(6).setCellValue(b.getUser().getEmail());
                row.createCell(7).setCellValue(b.getBookingKind());
            }
            Sheet util = wb.createSheet("Utilization");
            Row uh = util.createRow(0);
            uh.createCell(0).setCellValue("Resource");
            uh.createCell(1).setCellValue("Building");
            uh.createCell(2).setCellValue("Utilization %");
            int ur = 1;
            for (Map<String, Object> row : dashboardService.utilization(from, to)) {
                Row x = util.createRow(ur++);
                x.createCell(0).setCellValue(String.valueOf(row.get("name")));
                x.createCell(1).setCellValue(String.valueOf(row.get("building")));
                x.createCell(2).setCellValue(String.valueOf(row.get("utilization")));
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build Excel report", ex);
        }
    }

    public byte[] pdf(LocalDate from, LocalDate to) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("CampusOS Booking Report  " + from + " to " + to));
            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(6);
            table.addCell("Date");
            table.addCell("Time");
            table.addCell("Title");
            table.addCell("User");
            table.addCell("Status");
            table.addCell("Kind");
            for (Booking b : bookingRepository.findBetween(from, to)) {
                table.addCell(b.getBookingDate().toString());
                table.addCell(b.getStartTime() + "–" + b.getEndTime());
                table.addCell(b.getTitle() == null ? "" : b.getTitle());
                table.addCell(b.getUser().getEmail());
                table.addCell(b.getStatus().name());
                table.addCell(b.getBookingKind());
            }
            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build PDF report", ex);
        }
    }

    private String esc(String v) {
        if (v == null) return "";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
