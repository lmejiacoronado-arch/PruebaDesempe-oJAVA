package com.hotelnova.dao;
import com.hotelnova.model.entity.Reservation;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationDAO extends GenericDAO<Reservation, Integer> {
    List<Reservation> findActive();
    List<Reservation> findByGuestId(int guestId);
    List<Reservation> findByRoomId(int roomId);
    boolean hasOverlap(int roomId, LocalDate checkIn, LocalDate checkOut, int excludeId);
    Optional<Reservation> findActiveByRoomId(int roomId);
    // Transactional overloads
    Reservation save(Connection conn, Reservation r) throws SQLException;
    boolean update(Connection conn, Reservation r)   throws SQLException;
}
