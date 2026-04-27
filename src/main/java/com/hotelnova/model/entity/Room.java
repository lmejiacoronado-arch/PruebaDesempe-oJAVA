package com.hotelnova.model.entity;

import java.math.BigDecimal;

public class Room {
    private int id;
    private String roomNumber;
    private String roomType;
    private BigDecimal pricePerNight;
    private int capacity;
    private boolean available;
    private String description;

    public Room() {}
    public Room(int id, String roomNumber, String roomType, BigDecimal pricePerNight,
                int capacity, boolean available, String description) {
        this.id = id; this.roomNumber = roomNumber; this.roomType = roomType;
        this.pricePerNight = pricePerNight; this.capacity = capacity;
        this.available = available; this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("Room{id=%d, number='%s', type='%s', price=%s, available=%b}",
                id, roomNumber, roomType, pricePerNight, available);
    }
}
