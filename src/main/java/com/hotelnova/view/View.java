package com.hotelnova.view;
import com.hotelnova.model.entity.*;
import java.time.LocalDate;
import java.util.List;

public interface View {
    void    showMessage(String msg);
    void    showError(String msg);
    boolean confirm(String question);
    String  askInput(String prompt);
    LocalDate askDate(String prompt);
    int     showMenu(String[] options, String title);
    void    showRooms(List<Room> rooms);
    void    showRoom(Room room);
    void    showGuests(List<Guest> guests);
    void    showGuest(Guest guest);
    void    showReservations(List<Reservation> reservations);
    void    showReservation(Reservation reservation);
    void    showUsers(List<User> users);
}
