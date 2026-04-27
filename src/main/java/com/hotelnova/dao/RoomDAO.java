package com.hotelnova.dao;
import com.hotelnova.model.entity.Room;
import java.util.List;
import java.util.Optional;

public interface RoomDAO extends GenericDAO<Room, Integer> {
    Optional<Room> findByRoomNumber(String roomNumber);
    boolean existsByRoomNumber(String roomNumber);
    List<Room> findAvailable();
}
