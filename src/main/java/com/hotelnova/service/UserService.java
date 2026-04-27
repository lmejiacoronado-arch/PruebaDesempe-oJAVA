package com.hotelnova.service;

import com.hotelnova.dao.UserDAO;
import com.hotelnova.exception.*;
import com.hotelnova.model.entity.User;
import com.hotelnova.util.AppLogger;
import com.hotelnova.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class UserService {
    private static final AppLogger log = AppLogger.getInstance();
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) { this.userDAO = userDAO; }

    public Optional<User> authenticate(String username, String password) {
        return userDAO.findByUsername(username)
                .filter(u -> u.isActive() && PasswordUtil.verify(password, u.getPassword()));
    }

    public User create(String username, String password, String fullName, String role) {
        if (username == null || username.isBlank()) throw new ValidationException("El nombre de usuario es obligatorio");
        if (password == null || password.length() < 6) throw new ValidationException("La contraseña debe tener al menos 6 caracteres");
        if (userDAO.existsByUsername(username.trim())) throw new DuplicateException("nombre de usuario", username);
        User u = new User(0, username.trim(), PasswordUtil.hash(password), fullName, role, true);
        userDAO.save(u);
        log.info("User created — username: " + username);
        return u;
    }

    public List<User> findAll() { return userDAO.findAll(); }

    public void deactivate(int id) {
        User u = userDAO.findById(id).orElseThrow(() -> new NotFoundException("Usuario", id));
        u.setActive(false);
        userDAO.update(u);
        log.info("User deactivated — ID: " + id);
    }
}
