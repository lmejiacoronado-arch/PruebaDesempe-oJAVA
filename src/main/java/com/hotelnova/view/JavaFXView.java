package com.hotelnova.view;

import com.hotelnova.model.entity.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementación JavaFX de la interfaz View.
 * Usa diálogos nativos de JavaFX (Alert, TextInputDialog, ChoiceDialog).
 * Todos los métodos pueden llamarse desde cualquier hilo; internamente
 * redirige a Platform.runLater cuando es necesario y bloquea con un latch.
 */
public class JavaFXView implements View {

    // ── Utilidades de hilo ──────────────────────────────────────────────────

    /** Ejecuta un bloque en el FX thread y espera su resultado. */
    private <R> R runAndWait(java.util.function.Supplier<R> supplier) {
        if (Platform.isFxApplicationThread()) return supplier.get();
        AtomicReference<R> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { result.set(supplier.get()); latch.countDown(); });
        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return result.get();
    }

    private void runAndWait(Runnable r) {
        runAndWait(() -> { r.run(); return null; });
    }

    // ── Métodos de View ─────────────────────────────────────────────────────

    @Override
    public void showMessage(String msg) {
        runAndWait(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            a.setTitle("HotelNova — Información");
            a.setHeaderText(null);
            a.showAndWait();
        });
    }

    @Override
    public void showError(String msg) {
        runAndWait(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            a.setTitle("HotelNova — Error");
            a.setHeaderText(null);
            a.showAndWait();
        });
    }

    @Override
    public boolean confirm(String question) {
        return runAndWait(() -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, question, ButtonType.YES, ButtonType.NO);
            a.setTitle("Confirmar");
            a.setHeaderText(null);
            Optional<ButtonType> res = a.showAndWait();
            return res.isPresent() && res.get() == ButtonType.YES;
        });
    }

    @Override
    public String askInput(String prompt) {
        return runAndWait(() -> {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("HotelNova");
            dlg.setHeaderText(null);
            dlg.setContentText(prompt);
            return dlg.showAndWait().orElse("").trim();
        });
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
        return runAndWait(() -> {
            ChoiceDialog<String> dlg = new ChoiceDialog<>(options[0], options);
            dlg.setTitle("HotelNova — " + title);
            dlg.setHeaderText(null);
            dlg.setContentText("Seleccione una opción:");
            Optional<String> sel = dlg.showAndWait();
            if (sel.isEmpty()) return options.length; // último = volver
            for (int i = 0; i < options.length; i++) if (options[i].equals(sel.get())) return i + 1;
            return -1;
        });
    }

    // ── Métodos show* — todos usan un TextArea dentro de un Alert ───────────

    private void showTable(String title, String content) {
        runAndWait(() -> {
            TextArea ta = new TextArea(content);
            ta.setEditable(false);
            ta.setWrapText(false);
            ta.setPrefSize(700, 350);
            ta.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");

            VBox box = new VBox(ta);
            box.setPadding(new Insets(10));

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("HotelNova — " + title);
            a.setHeaderText(null);
            a.getDialogPane().setContent(box);
            a.getDialogPane().setPrefWidth(750);
            a.showAndWait();
        });
    }

    @Override
    public void showRooms(List<Room> rooms) {
        if (rooms.isEmpty()) { showMessage("No hay habitaciones registradas."); return; }
        StringBuilder sb = new StringBuilder(String.format("%-5s %-8s %-15s %-12s %-5s %-10s%n",
                "ID","#","Tipo","Precio/noche","Cap.","Disponible"));
        sb.append("─".repeat(60)).append("\n");
        for (Room r : rooms)
            sb.append(String.format("%-5d %-8s %-15s %-12s %-5d %-10s%n",
                    r.getId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerNight(),
                    r.getCapacity(), r.isAvailable() ? "✓ Sí" : "✗ No"));
        showTable("HABITACIONES", sb.toString());
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
        StringBuilder sb = new StringBuilder(String.format("%-5s %-15s %-25s %-25s %-6s%n",
                "ID","Documento","Nombre","Email","Activo"));
        sb.append("─".repeat(80)).append("\n");
        for (Guest g : guests)
            sb.append(String.format("%-5d %-15s %-25s %-25s %-6s%n",
                    g.getId(), g.getDocumentId(), g.getFullName(),
                    g.getEmail() != null ? g.getEmail() : "-", g.isActive() ? "Sí" : "No"));
        showTable("HUÉSPEDES", sb.toString());
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
        StringBuilder sb = new StringBuilder(String.format("%-5s %-22s %-6s %-12s %-12s %-10s %-10s%n",
                "ID","Huésped","Hab.","Check-in","Check-out","Estado","Total"));
        sb.append("─".repeat(85)).append("\n");
        for (Reservation r : list)
            sb.append(String.format("%-5d %-22s %-6s %-12s %-12s %-10s %-10s%n",
                    r.getId(), r.getGuestName() != null ? r.getGuestName() : "",
                    r.getRoomNumber() != null ? r.getRoomNumber() : "",
                    r.getCheckIn(), r.getCheckOut(), r.getStatus(), r.getTotalCost()));
        showTable("RESERVAS", sb.toString());
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
        StringBuilder sb = new StringBuilder(String.format("%-5s %-20s %-25s %-12s %-6s%n",
                "ID","Usuario","Nombre","Rol","Activo"));
        sb.append("─".repeat(70)).append("\n");
        for (User u : users)
            sb.append(String.format("%-5d %-20s %-25s %-12s %-6s%n",
                    u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.isActive() ? "Sí" : "No"));
        showTable("USUARIOS", sb.toString());
    }
}
