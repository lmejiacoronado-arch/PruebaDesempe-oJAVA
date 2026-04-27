package com.hotelnova.dao;
import com.hotelnova.model.entity.User;
import java.util.Optional;

public interface UserDAO extends GenericDAO<User, Integer> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
