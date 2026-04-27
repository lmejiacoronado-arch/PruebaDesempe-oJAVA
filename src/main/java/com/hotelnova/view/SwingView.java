package com.hotelnova.view;

import com.hotelnova.model.entity.*;
import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SwingView implements View {

    @Override
    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg, "HotelNova — Información", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "HotelNova — Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(null, question, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public String askInput(String prompt) {
        String result = JOptionPane.showInputDialog(null, prompt, "HotelNova", JOptionPane.QUESTION_MESSAGE);
        return result == null ? "" : result.trim();
    }

    @Override
    public LocalDate askDate(String prompt) {
        while (true) {
            String input = askInput(prompt + " (YYYY-MM-DD)");
            if (input.isBlank()) return null;
            try { return LocalDate.parse(input); }
            catch (DateTimeParseException e) { showError("Formato de fecha inválido. Use YYYY-MM-DD"); }
        }
    }

    @Override
    public int showMenu(String[] options, String title) {
        Object sel = JOptionPane.showInputDialog(null, "Seleccione una opción:",
                "HotelNova — " + title, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (sel == null) return options.length;
        for (int i = 0; i < options.length; i++) if (options[i].equals(sel)) return i + 1;
        return -1;
    }

    @Override
    public void showRooms(List<Room> rooms) {
        if (rooms.isEmpty()) { showMessage("No hay habitaciones registradas."); return; }
        StringBuilder sb = new StringBuilder("HABITACIONES\n");
        sb.append(String.format("%-5s %-8s %-15s %-12s %-5s %-10s%n","ID","#","Tipo","Precio/noche","Cap.","Disponible"));
        sb.append("─".repeat(60)).append("\n");
        for (Room r : rooms)
            sb.append(String.format("%-5d %-8s %-15s %-12s %-5d %-10s%n",
                    r.getId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerNight(),
                    r.getCapacity(), r.isAvailable() ? "✓ Sí" : "✗ No"));
        showMessage(sb.toString());
    }

    @Override
    public void showRoom(Room r) {
        showMessage(String.format("Habitación #%s\nTipo: %s\nPrecio/noche: $%s\nCapacidad: %d\nDisponible: %s\nDescripción: %s",
                r.getRoomNumber(), r.getRoomType(), r.getPricePerNight(),
                r.getCapacity(), r.isAvailable() ? "Sí" : "No",
                r.getDescription() != null ? r.getDescription() : "-"));
    }

    @Override
    public void showGuests(List<Guest> guests) {
        if (guests.isEmpty()) { showMessage("No hay huéspedes registrados."); return; }
        StringBuilder sb = new StringBuilder("HUÉSPEDES\n");
        sb.append(String.format("%-5s %-15s %-25s %-25s %-10s%n","ID","Documento","Nombre","Email","Activo"));
        sb.append("─".repeat(80)).append("\n");
        for (Guest g : guests)
            sb.append(String.format("%-5d %-15s %-25s %-25s %-10s%n",
                    g.getId(), g.getDocumentId(), g.getFullName(),
                    g.getEmail() != null ? g.getEmail() : "-", g.isActive() ? "Sí" : "No"));
        showMessage(sb.toString());
    }

    @Override
    public void showGuest(Guest g) {
        showMessage(String.format("Huésped\nID: %d\nDocumento: %s\nNombre: %s\nEmail: %s\nTeléfono: %s\nActivo: %s",
                g.getId(), g.getDocumentId(), g.getFullName(),
                g.getEmail(), g.getPhone(), g.isActive() ? "Sí" : "No"));
    }

    @Override
    public void showReservations(List<Reservation> list) {
        if (list.isEmpty()) { showMessage("No hay reservas."); return; }
        StringBuilder sb = new StringBuilder("RESERVAS\n");
        sb.append(String.format("%-5s %-25s %-8s %-12s %-12s %-12s %-12s%n",
                "ID","Huésped","Hab.","Check-in","Check-out","Estado","Total"));
        sb.append("─".repeat(90)).append("\n");
        for (Reservation r : list)
            sb.append(String.format("%-5d %-25s %-8s %-12s %-12s %-12s %-12s%n",
                    r.getId(), r.getGuestName() != null ? r.getGuestName() : "",
                    r.getRoomNumber() != null ? r.getRoomNumber() : "",
                    r.getCheckIn(), r.getCheckOut(), r.getStatus(), r.getTotalCost()));
        showMessage(sb.toString());
    }

    @Override
    public void showReservation(Reservation r) {
        showMessage(String.format("Reserva #%d\nHuésped: %s\nHabitación: %s\nCheck-in: %s\nCheck-out: %s\nEstado: %s\nTotal: $%s",
                r.getId(), r.getGuestName(), r.getRoomNumber(),
                r.getCheckIn(), r.getCheckOut(), r.getStatus(), r.getTotalCost()));
    }

    @Override
    public void showUsers(List<User> users) {
        if (users.isEmpty()) { showMessage("No hay usuarios."); return; }
        StringBuilder sb = new StringBuilder("USUARIOS\n");
        sb.append(String.format("%-5s %-20s %-30s %-15s %-8s%n","ID","Usuario","Nombre","Rol","Activo"));
        sb.append("─".repeat(80)).append("\n");
        for (User u : users)
            sb.append(String.format("%-5d %-20s %-30s %-15s %-8s%n",
                    u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.isActive() ? "Sí" : "No"));
        showMessage(sb.toString());
    }
}
