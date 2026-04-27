package com.hotelnova.service;

import com.hotelnova.dao.GuestDAO;
import com.hotelnova.exception.*;
import com.hotelnova.model.entity.Guest;
import com.hotelnova.util.AppLogger;

import java.util.List;

public class GuestService {
    private static final AppLogger log = AppLogger.getInstance();
    private final GuestDAO guestDAO;

    public GuestService(GuestDAO guestDAO) { this.guestDAO = guestDAO; }

    public Guest create(String documentId, String fullName, String email, String phone) {
        if (documentId == null || documentId.isBlank())
            throw new ValidationException("El documento de identidad es obligatorio");
        if (fullName == null || fullName.isBlank())
            throw new ValidationException("El nombre completo es obligatorio");
        if (guestDAO.existsByDocumentId(documentId.trim()))
            throw new DuplicateException("documento de identidad", documentId);

        Guest guest = new Guest(0, documentId.trim(), fullName.trim(), email, phone, true);
        guestDAO.save(guest);
        log.info("Guest created — doc: " + documentId + " ID: " + guest.getId());
        return guest;
    }

    public List<Guest> findAll()         { return guestDAO.findAll(); }
    public List<Guest> findByName(String name) { return guestDAO.findByName(name); }

    public Guest findById(int id) {
        return guestDAO.findById(id).orElseThrow(() -> new NotFoundException("Huésped", id));
    }

    public Guest update(int id, String fullName, String email, String phone) {
        Guest existing = guestDAO.findById(id).orElseThrow(() -> new NotFoundException("Huésped", id));
        if (fullName != null && !fullName.isBlank()) existing.setFullName(fullName.trim());
        if (email != null && !email.isBlank()) existing.setEmail(email.trim());
        if (phone != null && !phone.isBlank()) existing.setPhone(phone.trim());
        guestDAO.update(existing);
        log.info("Guest updated — ID: " + id);
        return existing;
    }

    public void deactivate(int id) {
        Guest g = guestDAO.findById(id).orElseThrow(() -> new NotFoundException("Huésped", id));
        g.setActive(false);
        guestDAO.update(g);
        log.info("Guest deactivated — ID: " + id);
    }
}
