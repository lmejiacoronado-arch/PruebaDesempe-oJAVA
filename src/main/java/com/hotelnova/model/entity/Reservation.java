package com.hotelnova.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Reservation {
    private int id;
    private int guestId;
    private int roomId;
    private int userId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String status;
    private BigDecimal totalCost;
    private String notes;
    // Denormalized for display
    private String guestName;
    private String roomNumber;

    public Reservation() {}
    public Reservation(int id, int guestId, int roomId, int userId,
                       LocalDate checkIn, LocalDate checkOut,
                       String status, BigDecimal totalCost, String notes) {
        this.id = id; this.guestId = guestId; this.roomId = roomId; this.userId = userId;
        this.checkIn = checkIn; this.checkOut = checkOut; this.status = status;
        this.totalCost = totalCost; this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId) { this.guestId = guestId; }
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    @Override
    public String toString() {
        return String.format("Reservation{id=%d, guest=%d, room=%d, checkIn=%s, checkOut=%s, status='%s', total=%s}",
                id, guestId, roomId, checkIn, checkOut, status, totalCost);
    }
}
