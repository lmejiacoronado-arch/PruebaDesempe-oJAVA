package com.hotelnova.controller;

import com.hotelnova.exception.AppException;
import com.hotelnova.model.entity.User;
import com.hotelnova.report.CsvReportService;
import com.hotelnova.service.*;
import com.hotelnova.view.View;

import java.util.Optional;

/**
 * Main orchestrator.
 * Handles login and delegates to each module controller.
 */
public class AppController {

    private final View                view;
    private final UserService         userService;
    private final RoomService         roomService;
    private final GuestService        guestService;
    private final ReservationService  reservationService;
    private final CsvReportService    csvReportService;

    private User loggedUser = null;

    public AppController(View view, UserService userService, RoomService roomService,
                         GuestService guestService, ReservationService reservationService,
                         CsvReportService csvReportService) {
        this.view               = view;
        this.userService        = userService;
        this.roomService        = roomService;
        this.guestService       = guestService;
        this.reservationService = reservationService;
        this.csvReportService   = csvReportService;
    }

    public void run() {
        if (!login()) return;

        String[] opts = {
            "🛏  Habitaciones",
            "👤  Huéspedes",
            "📋  Reservas",
            "📊  Exportar habitaciones CSV",
            "⚙️  Usuarios (solo ADMIN)",
            "🚪  Cerrar sesión"
        };

        boolean running = true;
        while (running) {
            switch (view.showMenu(opts, "HotelNova — " + loggedUser.getFullName())) {
                case 1 -> new RoomController(view, roomService).run();
                case 2 -> new GuestController(view, guestService).run();
                case 3 -> new ReservationController(view, reservationService, csvReportService)
                              .run(loggedUser.getId());
                case 4 -> exportRoomsCsv();
                case 5 -> {
                    if ("ADMIN".equals(loggedUser.getRole()))
                        new UserController(view, userService).run();
                    else
                        view.showError("Acceso denegado. Solo administradores.");
                }
                default -> running = false;
            }
        }
        view.showMessage("Sesión cerrada. ¡Hasta luego, " + loggedUser.getFullName() + "!");
    }

    private boolean login() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            String username = view.askInput("Usuario");
            String password = view.askInput("Contraseña");
            try {
                Optional<User> user = userService.authenticate(username, password);
                if (user.isPresent()) {
                    loggedUser = user.get();
                    view.showMessage("Bienvenido/a, " + loggedUser.getFullName()
                            + " [" + loggedUser.getRole() + "]");
                    return true;
                }
                view.showError("Credenciales incorrectas. Intento " + attempt + " de 3.");
            } catch (AppException e) {
                view.showError(e.getMessage());
            }
        }
        view.showError("Demasiados intentos fallidos. La aplicación se cerrará.");
        return false;
    }

    private void exportRoomsCsv() {
        try {
            String file = csvReportService.exportRooms(roomService.findAll());
            view.showMessage("Reporte de habitaciones exportado:\n" + file);
        } catch (AppException e) { view.showError(e.getMessage()); }
    }
}
