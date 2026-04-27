package com.hotelnova.controller;

import com.hotelnova.exception.AppException;
import com.hotelnova.service.GuestService;
import com.hotelnova.view.View;

public class GuestController {
    private final View view;
    private final GuestService guestService;

    public GuestController(View view, GuestService guestService) {
        this.view = view; this.guestService = guestService;
    }

    public void run() {
        String[] opts = {"Listar huéspedes","Buscar por nombre","Ver por ID",
                         "Registrar huésped","Actualizar huésped","Desactivar huésped","Volver"};
        boolean running = true;
        while (running) {
            switch (view.showMenu(opts, "Gestión de Huéspedes")) {
                case 1 -> view.showGuests(guestService.findAll());
                case 2 -> { String n = view.askInput("Nombre"); view.showGuests(guestService.findByName(n)); }
                case 3 -> findById();
                case 4 -> create();
                case 5 -> update();
                case 6 -> deactivate();
                default -> running = false;
            }
        }
    }

    private void findById() {
        try { int id = Integer.parseInt(view.askInput("ID del huésped")); view.showGuest(guestService.findById(id));
        } catch (NumberFormatException e) { view.showError("ID inválido");
        } catch (AppException e) { view.showError(e.getMessage()); }
    }

    private void create() {
        try {
            String doc   = view.askInput("Número de documento");
            String name  = view.askInput("Nombre completo");
            String email = view.askInput("Email (opcional)");
            String phone = view.askInput("Teléfono (opcional)");
            guestService.create(doc, name, email, phone);
            view.showMessage("Huésped registrado exitosamente.");
        } catch (AppException e) { view.showError(e.getMessage()); }
    }

    private void update() {
        try {
            int id = Integer.parseInt(view.askInput("ID del huésped a actualizar"));
            view.showGuest(guestService.findById(id));
            String name  = view.askInput("Nuevo nombre (Enter para omitir)");
            String email = view.askInput("Nuevo email (Enter para omitir)");
            String phone = view.askInput("Nuevo teléfono (Enter para omitir)");
            guestService.update(id, name, email, phone);
            view.showMessage("Huésped actualizado.");
        } catch (NumberFormatException e) { view.showError("ID inválido");
        } catch (AppException e) { view.showError(e.getMessage()); }
    }

    private void deactivate() {
        try {
            int id = Integer.parseInt(view.askInput("ID del huésped a desactivar"));
            if (view.confirm("¿Desactivar huésped " + id + "?")) {
                guestService.deactivate(id); view.showMessage("Huésped desactivado.");
            }
        } catch (NumberFormatException e) { view.showError("ID inválido");
        } catch (AppException e) { view.showError(e.getMessage()); }
    }
}
