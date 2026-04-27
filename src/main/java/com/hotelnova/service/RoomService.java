package com.hotelnova.service;

import com.hotelnova.dao.ReservationDAO;
import com.hotelnova.dao.RoomDAO;
import com.hotelnova.exception.*;
import com.hotelnova.model.entity.Room;
import com.hotelnova.util.AppLogger;

import java.math.BigDecimal;
import java.util.List;

public class RoomService {
    private static final AppLogger log = AppLogger.getInstance();
    private final RoomDAO roomDAO;
    private final ReservationDAO reservationDAO;

    public RoomService(RoomDAO roomDAO, ReservationDAO reservationDAO) {
        this.roomDAO = roomDAO;
        this.reservationDAO = reservationDAO;
    }

    public Room create(String roomNumber, String roomType, BigDecimal pricePerNight, int capacity, String description) {
        if (roomNumber == null || roomNumber.isBlank())
            throw new ValidationException("El número de habitación no puede estar vacío");
        if (roomType == null || roomType.isBlank())
            throw new ValidationException("El tipo de habitación es obligatorio");
        if (pricePerNight == null || pricePerNight.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException("El precio por noche debe ser mayor a cero");
        if (capacity <= 0)
            throw new ValidationException("La capacidad debe ser mayor a cero");
        if (roomDAO.existsByRoomNumber(roomNumber.trim()))
            throw new DuplicateException("número de habitación", roomNumber);

        Room room = new Room(0, roomNumber.trim(), roomType, pricePerNight, capacity, true, description);
        roomDAO.save(room);
        log.info("Room created — number: " + roomNumber + " ID: " + room.getId());
        return room;
    }

    public List<Room> findAll()       { return roomDAO.findAll(); }
    public List<Room> findAvailable() { return roomDAO.findAvailable(); }

    public Room findById(int id) {
        return roomDAO.findById(id).orElseThrow(() -> new NotFoundException("Habitación", id));
    }

    public Room update(int id, String roomNumber, String roomType, BigDecimal pricePerNight, int capacity, String description) {
        Room existing = roomDAO.findById(id).orElseThrow(() -> new NotFoundException("Habitación", id));
        if (roomNumber != null && !roomNumber.isBlank()) {
            if (!roomNumber.trim().equalsIgnoreCase(existing.getRoomNumber())
                    && roomDAO.existsByRoomNumber(roomNumber.trim()))
                throw new DuplicateException("número de habitación", roomNumber);
            existing.setRoomNumber(roomNumber.trim());
        }
        if (roomType != null && !roomType.isBlank()) existing.setRoomType(roomType);
        if (pricePerNight != null && pricePerNight.compareTo(BigDecimal.ZERO) > 0) existing.setPricePerNight(pricePerNight);
        if (capacity > 0) existing.setCapacity(capacity);
        if (description != null) existing.setDescription(description);
        roomDAO.update(existing);
        log.info("Room updated — ID: " + id);
        return existing;
    }

    public void delete(int id) {
        roomDAO.findById(id).orElseThrow(() -> new NotFoundException("Habitación", id));
        // Problema 4 corregido: verificar que no haya reservas activas antes de eliminar
        if (reservationDAO.findActiveByRoomId(id).isPresent())
            throw new ValidationException("No se puede eliminar la habitación porque tiene una reserva activa.");
        roomDAO.deleteById(id);
        log.info("Room deleted — ID: " + id);
    }
}
