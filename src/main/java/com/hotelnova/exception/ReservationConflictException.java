package com.hotelnova.exception;
public class ReservationConflictException extends AppException {
    public ReservationConflictException(String roomNumber) {
        super("Room " + roomNumber + " has an overlapping reservation for the selected dates");
    }
}
