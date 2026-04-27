package com.hotelnova.controller;

import com.hotelnova.exception.AppException;
import com.hotelnova.report.CsvReportService;
import com.hotelnova.service.ReservationService;
import com.hotelnova.view.View;

import java.time.LocalDate;

public class ReservationController {
    private final View               view;
    private final ReservationService reservationService;
    private final CsvReportService   csvReportService;

    public ReservationController(View view, ReservationService reservationService,
                                 CsvReportService csvReportService) {
        this.view               = view;
        this.reservationService = reservationService;
        this.csvReportService   = csvReportService;
    }

    public void run(int loggedUserId) {
        String[] opts = {
            "Listar todas las reservas", "Reservas activas",
            "Ver reserva por ID", "Nueva reserva",
            "Check-out", "Exportar reservas activas a CSV", "Volver"
        };
        boolean running = true;
        while (running) {
            switch (view.showMenu(opts, "Gestión de Reservas")) {
                case 1 -> view.showReservations(reservationService.findAll());
                case 2 -> view.showReservations(reservationService.findActive());
                case 3 -> findById();
                case 4 -> create(loggedUserId);
                case 5 -> checkOut();
                case 6 -> exportCsv();
                default -> running = false;
            }
        }
    }

    private void findById() {
        try {
            int id = Integer.parseInt(view.askInput("ID de la reserva"));
            view.showReservation(reservationService.findById(id));
        } catch (NumberFormatException e) { view.showError("ID inválido");
        } catch (AppException e)          { view.showError(e.getMessage()); }
    }

    private void create(int loggedUserId) {
        try {
            int guestId  = Integer.parseInt(view.askInput("ID del huésped"));
            int roomId   = Integer.parseInt(view.askInput("ID de la habitación"));
            LocalDate ci = view.askDate("Fecha de check-in");
            LocalDate co = view.askDate("Fecha de check-out");
            String notes = view.askInput("Notas (opcional)");

            var res = reservationService.create(guestId, roomId, loggedUserId, ci, co, notes);
            view.showMessage("Reserva creada exitosamente.\nID: " + res.getId()
                    + "\nTotal (con IVA): $" + res.getTotalCost());
        } catch (NumberFormatException e) { view.showError("ID inválido");
        } catch (AppException e)          { view.showError(e.getMessage()); }
    }

    private void checkOut() {
        try {
            int id  = Integer.parseInt(view.askInput("ID de la reserva a hacer check-out"));
            var res = reservationService.findById(id);
            view.showReservation(res);
            if (view.confirm("¿Confirmar check-out?")) {
                reservationService.checkOut(id);
                view.showMessage("Check-out realizado. Habitación liberada.");
            }
        } catch (NumberFormatException e) { view.showError("ID inválido");
        } catch (AppException e)          { view.showError(e.getMessage()); }
    }

    private void exportCsv() {
        try {
            var active = reservationService.findActive();
            String file = csvReportService.exportActiveReservations(active);
            view.showMessage("Reporte exportado: " + file);
        } catch (AppException e) { view.showError(e.getMessage()); }
    }
}
