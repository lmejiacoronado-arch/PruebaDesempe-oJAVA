package com.hotelnova.view;

import com.hotelnova.model.entity.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleView implements View {

    private final Scanner sc = new Scanner(System.in);

    private void line(int n) { System.out.println("─".repeat(n)); }
    private void header(String title) {
        line(60);
        System.out.printf("  HotelNova — %s%n", title);
        line(60);
    }

    @Override
    public void showMessage(String msg) {
        System.out.println("\n[INFO] " + msg);
    }

    @Override
    public void showError(String msg) {
        System.out.println("\n[ERROR] " + msg);
    }

    @Override
    public boolean confirm(String question) {
        System.out.print("\n" + question + " (s/n): ");
        return sc.nextLine().trim().equalsIgnoreCase("s");
    }

    @Override
    public String askInput(String prompt) {
        System.out.print(prompt + ": ");
        return sc.nextLine().trim();
    }

    @Override
    public LocalDate askDate(String prompt) {
        while (true) {
            String input = askInput(prompt + " (YYYY-MM-DD)");
            if (input.isBlank()) return null;
            try { return LocalDate.parse(input); }
            catch (DateTimeParseException e) { showError("Formato inválido. Use YYYY-MM-DD"); }
        }
    }

    @Override
    public int showMenu(String[] options, String title) {
        header(title);
        for (int i = 0; i < options.length; i++)
            System.out.printf("  %d. %s%n", i + 1, options[i]);
        line(60);
        while (true) {
            System.out.print("Opción: ");
            try {
                int choice = Integer.parseInt(sc.nextLine().trim());
                if (choice >= 1 && choice <= options.length) return choice;
                showError("Ingrese un número entre 1 y " + options.length);
            } catch (NumberFormatException e) { showError("Ingrese un número válido"); }
        }
    }

    @Override
    public void showRooms(List<Room> rooms) {
        if (rooms.isEmpty()) { showMessage("No hay habitaciones registradas."); return; }
        header("HABITACIONES");
        System.out.printf("%-5s %-8s %-15s %-12s %-5s %-10s%n","ID","#","Tipo","Precio/noche","Cap.","Disponible");
        line(60);
        for (Room r : rooms)
            System.out.printf("%-5d %-8s %-15s %-12s %-5d %-10s%n",
                    r.getId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerNight(),
                    r.getCapacity(), r.isAvailable() ? "✓ Sí" : "✗ No");
    }

    @Override
    public void showRoom(Room r) {
        header("HABITACIÓN #" + r.getRoomNumber());
        System.out.printf("Tipo: %s | Precio: $%s | Capacidad: %d | Disponible: %s%n",
                r.getRoomType(), r.getPricePerNight(), r.getCapacity(), r.isAvailable() ? "Sí" : "No");
        if (r.getDescription() != null) System.out.println("Descripción: " + r.getDescription());
    }

    @Override
    public void showGuests(List<Guest> guests) {
        if (guests.isEmpty()) { showMessage("No hay huéspedes registrados."); return; }
        header("HUÉSPEDES");
        System.out.printf("%-5s %-15s %-25s %-25s %-6s%n","ID","Documento","Nombre","Email","Activo");
        line(80);
        for (Guest g : guests)
            System.out.printf("%-5d %-15s %-25s %-25s %-6s%n",
                    g.getId(), g.getDocumentId(), g.getFullName(),
                    g.getEmail() != null ? g.getEmail() : "-", g.isActive() ? "Sí" : "No");
    }

    @Override
    public void showGuest(Guest g) {
        header("HUÉSPED");
        System.out.printf("ID: %d | Doc: %s | Nombre: %s | Email: %s | Tel: %s | Activo: %s%n",
                g.getId(), g.getDocumentId(), g.getFullName(),
                g.getEmail(), g.getPhone(), g.isActive() ? "Sí" : "No");
    }

    @Override
    public void showReservations(List<Reservation> list) {
        if (list.isEmpty()) { showMessage("No hay reservas."); return; }
        header("RESERVAS");
        System.out.printf("%-5s %-22s %-6s %-12s %-12s %-10s %-10s%n",
                "ID","Huésped","Hab.","Check-in","Check-out","Estado","Total");
        line(85);
        for (Reservation r : list)
            System.out.printf("%-5d %-22s %-6s %-12s %-12s %-10s %-10s%n",
                    r.getId(), r.getGuestName() != null ? r.getGuestName() : "",
                    r.getRoomNumber() != null ? r.getRoomNumber() : "",
                    r.getCheckIn(), r.getCheckOut(), r.getStatus(), r.getTotalCost());
    }

    @Override
    public void showReservation(Reservation r) {
        header("RESERVA #" + r.getId());
        System.out.printf("Huésped: %s | Hab: %s | Check-in: %s | Check-out: %s | Estado: %s | Total: $%s%n",
                r.getGuestName(), r.getRoomNumber(),
                r.getCheckIn(), r.getCheckOut(), r.getStatus(), r.getTotalCost());
    }

    @Override
    public void showUsers(List<User> users) {
        if (users.isEmpty()) { showMessage("No hay usuarios."); return; }
        header("USUARIOS");
        System.out.printf("%-5s %-20s %-25s %-12s %-6s%n","ID","Usuario","Nombre","Rol","Activo");
        line(70);
        for (User u : users)
            System.out.printf("%-5d %-20s %-25s %-12s %-6s%n",
                    u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.isActive() ? "Sí" : "No");
    }
}
