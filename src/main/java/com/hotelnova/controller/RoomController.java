package com.hotelnova.controller;

import com.hotelnova.exception.AppException;
import com.hotelnova.service.RoomService;
import com.hotelnova.view.View;

import java.math.BigDecimal;

public class RoomController {
    private final View view;
    private final RoomService roomService;

    public RoomController(View view, RoomService roomService) {
        this.view = view; this.roomService = roomService;
    }

    public void run() {
        String[] opts = {"Listar habitaciones","Habitaciones disponibles","Ver por ID",
                         "Registrar habitación","Actualizar habitación","Eliminar habitación","Volver"};
        boolean running = true;
        while (running) {
            switch (view.showMenu(opts, "Gestión de Habitaciones")) {
                case 1 -> view.showRooms(roomService.findAll());
                case 2 -> view.showRooms(roomService.findAvailable());
                case 3 -> findById();
                case 4 -> create();
                case 5 -> update();
                case 6 -> delete();
                default -> running = false;
            }
        }
    }

    private void findById() {
        try {
            int id = Integer.parseInt(view.askInput("ID de la habitación"));
            view.showRoom(roomService.findById(id));
        } catch (NumberFormatException e) { view.showError("ID inválido");
        } catch (AppException e) { view.showError(e.getMessage()); }
    }

    private void create() {
        try {
            String number  = view.askInput("Número de habitación");
            String[] types = {"SINGLE","DOUBLE","SUITE","PRESIDENTIAL"};
            String type    = types[view.showMenu(types, "Tipo de habitación") - 1];
            BigDecimal price = new BigDecimal(view.askInput("Precio por noche"));
            int capacity   = Integer.parseInt(view.askInput("Capacidad (personas)"));
            String desc    = view.askInput("Descripción (opcional)");
            roomService.create(number, type, price, capacity, desc);
            view.showMessage("Habitación registrada exitosamente.");
        } catch (NumberFormatException e) { view.showError("Precio o capacidad inválidos");
        } catch (AppException e) { view.showError(e.getMessage()); }
    }

    private void update() {
        try {
            int id = Integer.parseInt(view.askInput("ID de la habitación a actualizar"));
            view.showRoom(roomService.findById(id));

            String number = view.askInput("Nuevo número (Enter para omitir)");
            // Problema 6 corregido: ahora se permite actualizar el tipo de habitación
            String[] typeOpts = {"(Sin cambio)","SINGLE","DOUBLE","SUITE","PRESIDENTIAL"};
            int typeChoice    = view.showMenu(typeOpts, "Nuevo tipo de habitación") - 1;
            String type       = typeChoice == 0 ? null : typeOpts[typeChoice];
            String price      = view.askInput("Nuevo precio/noche (Enter para omitir)");
            String cap        = view.askInput("Nueva capacidad (Enter para omitir)");
            String desc       = view.askInput("Nueva descripción (Enter para omitir)");

            BigDecimal p = price.isBlank() ? null : new BigDecimal(price);
            int c        = cap.isBlank() ? 0 : Integer.parseInt(cap);
            roomService.update(id, number, type, p, c, desc.isBlank() ? null : desc);
            view.showMessage("Habitación actualizada.");
        } catch (NumberFormatException e) { view.showError("Dato inválido");
        } catch (AppException e) { view.showError(e.getMessage()); }
    }

    private void delete() {
        try {
            int id = Integer.parseInt(view.askInput("ID de la habitación a eliminar"));
            if (view.confirm("¿Confirmar eliminación?")) {
                roomService.delete(id);
                view.showMessage("Habitación eliminada.");
            }
        } catch (NumberFormatException e) { view.showError("ID inválido");
        } catch (AppException e) { view.showError(e.getMessage()); }
    }
}
