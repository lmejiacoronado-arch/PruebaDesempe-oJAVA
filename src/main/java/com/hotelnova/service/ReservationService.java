package com.hotelnova.service;

import com.hotelnova.config.AppConfig;
import com.hotelnova.dao.GuestDAO;
import com.hotelnova.dao.ReservationDAO;
import com.hotelnova.dao.RoomDAO;
import com.hotelnova.db.ConnectionManager;
import com.hotelnova.exception.*;
import com.hotelnova.model.entity.*;
import com.hotelnova.util.AppLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service that orchestrates the Reservation flow.
 * Owns ALL business rules and ALL transaction control.
 *
 * Critical operations (checkIn, checkOut) use explicit JDBC transactions.
 */
public class ReservationService {

    private static final AppLogger log = AppLogger.getInstance();

    private final ReservationDAO reservationDAO;
    private final RoomDAO        roomDAO;
    private final GuestDAO       guestDAO;
    private final AppConfig      config = AppConfig.getInstance();

    public ReservationService(ReservationDAO reservationDAO, RoomDAO roomDAO, GuestDAO guestDAO) {
        this.reservationDAO = reservationDAO;
        this.roomDAO        = roomDAO;
        this.guestDAO       = guestDAO;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BR-001  guest must be active
    // BR-002  room must be available
    // BR-003  checkIn < checkOut
    // BR-004  no overlapping reservations for same room
    // ─────────────────────────────────────────────────────────────────────────
    public Reservation create(int guestId, int roomId, int userId,
                              LocalDate checkIn, LocalDate checkOut, String notes) {
        // BR-003
        if (checkIn == null || checkOut == null)
            throw new ValidationException("Las fechas de check-in y check-out son obligatorias");
        if (!checkOut.isAfter(checkIn))
            throw new ValidationException("La fecha de check-out debe ser posterior al check-in");
        if (checkIn.isBefore(LocalDate.now()))
            throw new ValidationException("La fecha de check-in no puede ser en el pasado");

        // BR-001
        Guest guest = guestDAO.findById(guestId)
                .orElseThrow(() -> new NotFoundException("Huésped", guestId));
        if (!guest.isActive())
            throw new ValidationException("El huésped con ID " + guestId + " no está activo");

        // BR-002
        Room room = roomDAO.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Habitación", roomId));
        if (!room.isAvailable())
            throw new RoomNotAvailableException(room.getRoomNumber());

        // BR-004
        if (reservationDAO.hasOverlap(roomId, checkIn, checkOut, 0))
            throw new ReservationConflictException(room.getRoomNumber());

        BigDecimal totalCost = calculateCost(room.getPricePerNight(), checkIn, checkOut);

        // ── Transactional: save reservation + mark room unavailable ──
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            Reservation res = new Reservation(0, guestId, roomId, userId,
                    checkIn, checkOut, "ACTIVE", totalCost, notes);
            reservationDAO.save(conn, res);

            room.setAvailable(false);
            // direct update via roomDAO (no transactional overload needed for single field)
            // use same connection via direct PS
            try (var ps = conn.prepareStatement("UPDATE rooms SET available=false WHERE id=?")) {
                ps.setInt(1, roomId); ps.executeUpdate();
            }

            conn.commit();
            log.info("Reservation created — ID: " + res.getId() + " room: " + room.getRoomNumber()
                    + " guest: " + guest.getFullName() + " total: " + totalCost);
            return res;

        } catch (AppException e) {
            rollback(conn); log.warn("Reservation creation rolled back — " + e.getMessage()); throw e;
        } catch (SQLException e) {
            rollback(conn); log.error("DB error in create reservation — " + e.getMessage());
            throw new DatabaseException("Error al crear la reserva", e);
        } finally {
            restore(conn);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BR-005  cannot check-out without an active reservation
    // ─────────────────────────────────────────────────────────────────────────
    public Reservation checkOut(int reservationId) {
        Reservation res = reservationDAO.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reserva", reservationId));

        if (!"ACTIVE".equals(res.getStatus()))
            throw new CheckoutException("No se puede hacer check-out de una reserva con estado: " + res.getStatus());

        Room room = roomDAO.findById(res.getRoomId())
                .orElseThrow(() -> new NotFoundException("Habitación", res.getRoomId()));

        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            res.setStatus("CHECKED_OUT");
            reservationDAO.update(conn, res);

            try (var ps = conn.prepareStatement("UPDATE rooms SET available=true WHERE id=?")) {
                ps.setInt(1, room.getId()); ps.executeUpdate();
            }

            conn.commit();
            log.info("Check-out completed — reservation ID: " + reservationId
                    + " room: " + room.getRoomNumber());
            return res;

        } catch (AppException e) {
            rollback(conn); log.warn("CheckOut rolled back — " + e.getMessage()); throw e;
        } catch (SQLException e) {
            rollback(conn); log.error("DB error in checkOut — " + e.getMessage());
            throw new DatabaseException("Error al realizar el check-out", e);
        } finally {
            restore(conn);
        }
    }

    public List<Reservation> findAll()    { return reservationDAO.findAll(); }
    public List<Reservation> findActive() { return reservationDAO.findActive(); }

    public Reservation findById(int id) {
        return reservationDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva", id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CALC-001  nights × price_per_night × (1 + tax_rate)
    // ─────────────────────────────────────────────────────────────────────────
    public BigDecimal calculateCost(BigDecimal pricePerNight, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) throw new ValidationException("Las fechas no producen noches válidas");
        BigDecimal subtotal = pricePerNight.multiply(BigDecimal.valueOf(nights));
        BigDecimal taxRate  = config.getTaxRate();
        return subtotal.multiply(BigDecimal.ONE.add(taxRate)).setScale(2, RoundingMode.HALF_UP);
    }

    // ── Transaction helpers ──
    private void rollback(Connection conn) {
        if (conn != null) try { conn.rollback(); }
        catch (SQLException ex) { log.error("Rollback failed: " + ex.getMessage()); }
    }
    private void restore(Connection conn) {
        if (conn != null) try { conn.setAutoCommit(true); conn.close(); }
        catch (SQLException ex) { log.error("Connection restore failed: " + ex.getMessage()); }
    }
}
