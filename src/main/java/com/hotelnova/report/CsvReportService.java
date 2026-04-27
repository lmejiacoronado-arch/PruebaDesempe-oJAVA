package com.hotelnova.report;

import com.hotelnova.config.AppConfig;
import com.hotelnova.exception.AppException;
import com.hotelnova.model.entity.Reservation;
import com.hotelnova.model.entity.Room;
import com.hotelnova.util.AppLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates CSV reports to the configured reports/ directory.
 */
public class CsvReportService {
    private static final AppLogger log = AppLogger.getInstance();
    private final AppConfig config = AppConfig.getInstance();
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public String exportRooms(List<Room> rooms) {
        String filename = config.getReportDir() + "rooms_" + LocalDateTime.now().format(DT) + ".csv";
        new File(config.getReportDir()).mkdirs();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            pw.println("id,room_number,room_type,price_per_night,capacity,available,description");
            for (Room r : rooms) {
                pw.printf("%d,%s,%s,%s,%d,%b,%s%n",
                        r.getId(), r.getRoomNumber(), r.getRoomType(),
                        r.getPricePerNight(), r.getCapacity(), r.isAvailable(),
                        r.getDescription() != null ? r.getDescription().replace(",", ";") : "");
            }
            log.info("Rooms CSV exported — file: " + filename + " rows: " + rooms.size());
        } catch (IOException e) {
            log.error("Error exporting rooms CSV: " + e.getMessage());
            throw new AppException("No se pudo exportar el reporte de habitaciones", e);
        }
        return filename;
    }

    public String exportActiveReservations(List<Reservation> reservations) {
        String filename = config.getReportDir() + "active_reservations_" + LocalDateTime.now().format(DT) + ".csv";
        new File(config.getReportDir()).mkdirs();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            pw.println("id,guest_name,room_number,check_in,check_out,status,total_cost");
            for (Reservation r : reservations) {
                pw.printf("%d,%s,%s,%s,%s,%s,%s%n",
                        r.getId(),
                        r.getGuestName() != null ? r.getGuestName() : "",
                        r.getRoomNumber() != null ? r.getRoomNumber() : "",
                        r.getCheckIn(), r.getCheckOut(),
                        r.getStatus(), r.getTotalCost());
            }
            log.info("Reservations CSV exported — file: " + filename + " rows: " + reservations.size());
        } catch (IOException e) {
            log.error("Error exporting reservations CSV: " + e.getMessage());
            throw new AppException("No se pudo exportar el reporte de reservas", e);
        }
        return filename;
    }
}
