package com.hotelnova.controller;

import com.hotelnova.exception.AppException;
import com.hotelnova.service.UserService;
import com.hotelnova.view.View;

public class UserController {
    private final View        view;
    private final UserService userService;

    public UserController(View view, UserService userService) {
        this.view = view; this.userService = userService;
    }

    public void run() {
        String[] opts = {"Listar usuarios", "Crear usuario", "Desactivar usuario", "Volver"};
        boolean running = true;
        while (running) {
            switch (view.showMenu(opts, "Gestión de Usuarios")) {
                case 1 -> view.showUsers(userService.findAll());
                case 2 -> create();
                case 3 -> deactivate();
                default -> running = false;
            }
        }
    }

    private void create() {
        try {
            String username = view.askInput("Nombre de usuario");
            String password = view.askInput("Contraseña (mín. 6 caracteres)");
            String fullName = view.askInput("Nombre completo");
            String[] roles  = {"ADMIN", "RECEPTIONIST"};
            String role     = roles[view.showMenu(roles, "Rol") - 1];
            userService.create(username, password, fullName, role);
            view.showMessage("Usuario creado exitosamente.");
        } catch (AppException e) { view.showError(e.getMessage()); }
    }

    private void deactivate() {
        try {
            int id = Integer.parseInt(view.askInput("ID del usuario a desactivar"));
            if (view.confirm("¿Desactivar usuario " + id + "?")) {
                userService.deactivate(id);
                view.showMessage("Usuario desactivado.");
            }
        } catch (NumberFormatException e) { view.showError("ID inválido");
        } catch (AppException e)          { view.showError(e.getMessage()); }
    }
}
