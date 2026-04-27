package com.hotelnova;

import com.hotelnova.config.AppConfig;
import com.hotelnova.controller.AppController;
import com.hotelnova.dao.*;
import com.hotelnova.dao.impl.*;
import com.hotelnova.report.CsvReportService;
import com.hotelnova.service.*;
import com.hotelnova.util.AppLogger;
import com.hotelnova.view.*;
import javafx.application.Platform;

import javax.swing.*;

/**
 * Entry point.
 * Al arrancar pregunta qué interfaz usar: Swing (JOptionPane), JavaFX o Consola.
 * Luego ensambla el grafo completo de dependencias (Composition Root).
 */
public class Main {

    public static void main(String[] args) {
        AppLogger.getInstance();
        AppConfig.getInstance();

        View view = selectView();

        // ── DAOs ──
        UserDAO        userDAO        = new UserDAOImpl();
        GuestDAO       guestDAO       = new GuestDAOImpl();
        RoomDAO        roomDAO        = new RoomDAOImpl();
        ReservationDAO reservationDAO = new ReservationDAOImpl();

        // ── Services ──
        UserService        userService        = new UserService(userDAO);
        RoomService        roomService        = new RoomService(roomDAO, reservationDAO);
        GuestService       guestService       = new GuestService(guestDAO);
        ReservationService reservationService = new ReservationService(reservationDAO, roomDAO, guestDAO);

        // ── Reports ──
        CsvReportService csvReportService = new CsvReportService();

        // ── Orchestrator ──
        AppController app = new AppController(view, userService, roomService,
                guestService, reservationService, csvReportService);
        app.run();

        // Apagar FX si se usó
        if (view instanceof JavaFXView) Platform.exit();
    }

    // ── Selección de interfaz ───────────────────────────────────────────────

    private static View selectView() {
        String[] options = {"Swing (JOptionPane)", "JavaFX", "Consola"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Seleccione la interfaz de usuario:",
                "HotelNova — Inicio",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        return switch (choice) {
            case 1 -> initJavaFX();
            case 2 -> new ConsoleView();
            default -> new SwingView();   // 0 = Swing, -1 = cerró la ventana
        };
    }

    private static JavaFXView initJavaFX() {
        // JavaFX necesita que el toolkit esté inicializado antes de usar diálogos
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Ya estaba inicializado
        }
        return new JavaFXView();
    }
}
