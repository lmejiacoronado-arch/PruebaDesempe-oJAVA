package com.hotelnova.exception;
public class RoomNotAvailableException extends AppException {
    public RoomNotAvailableException(String roomNumber) {
        super("Room " + roomNumber + " is not available for the selected dates");
    }
}
