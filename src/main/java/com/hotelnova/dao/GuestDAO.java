package com.hotelnova.dao;
import com.hotelnova.model.entity.Guest;
import java.util.List;
import java.util.Optional;

public interface GuestDAO extends GenericDAO<Guest, Integer> {
    Optional<Guest> findByDocumentId(String documentId);
    boolean existsByDocumentId(String documentId);
    List<Guest> findByName(String name);
}
