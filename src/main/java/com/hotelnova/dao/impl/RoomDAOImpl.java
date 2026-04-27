package com.hotelnova.dao.impl;

import com.hotelnova.dao.RoomDAO;
import com.hotelnova.exception.DatabaseException;
import com.hotelnova.model.entity.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDAOImpl extends GenericDAOImpl<Room, Integer> implements RoomDAO {

    private static final String INSERT      = "INSERT INTO rooms(room_number,room_type,price_per_night,capacity,available,description) VALUES(?,?,?,?,?,?) RETURNING id";
    private static final String UPDATE      = "UPDATE rooms SET room_number=?,room_type=?,price_per_night=?,capacity=?,available=?,description=? WHERE id=?";
    private static final String DELETE      = "DELETE FROM rooms WHERE id=?";
    private static final String FIND_BY_ID  = "SELECT * FROM rooms WHERE id=?";
    private static final String FIND_ALL    = "SELECT * FROM rooms ORDER BY room_number";
    private static final String FIND_BY_NUM = "SELECT * FROM rooms WHERE room_number=?";
    private static final String EXISTS_NUM  = "SELECT 1 FROM rooms WHERE room_number=? LIMIT 1";
    private static final String FIND_AVAIL  = "SELECT * FROM rooms WHERE available=true ORDER BY room_number";

    @Override protected String getInsertSQL()    { return INSERT; }
    @Override protected String getUpdateSQL()    { return UPDATE; }
    @Override protected String getDeleteSQL()    { return DELETE; }
    @Override protected String getFindByIdSQL()  { return FIND_BY_ID; }
    @Override protected String getFindAllSQL()   { return FIND_ALL; }

    @Override
    protected void setInsertParams(PreparedStatement ps, Room r) throws SQLException {
        ps.setString(1, r.getRoomNumber());        ps.setString(2, r.getRoomType());
        ps.setBigDecimal(3, r.getPricePerNight()); ps.setInt(4, r.getCapacity());
        ps.setBoolean(5, r.isAvailable());         ps.setString(6, r.getDescription());
    }

    @Override
    protected void setUpdateParams(PreparedStatement ps, Room r) throws SQLException {
        ps.setString(1, r.getRoomNumber());        ps.setString(2, r.getRoomType());
        ps.setBigDecimal(3, r.getPricePerNight()); ps.setInt(4, r.getCapacity());
        ps.setBoolean(5, r.isAvailable());         ps.setString(6, r.getDescription());
        ps.setInt(7, r.getId());
    }

    @Override
    protected void setDeleteParam(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }

    @Override
    protected void setFindByIdParam(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }

    @Override
    protected void setGeneratedId(Room r, ResultSet rs) throws SQLException {
        r.setId(rs.getInt("id"));
    }

    @Override
    protected Room mapRow(ResultSet rs) throws SQLException {
        return new Room(rs.getInt("id"), rs.getString("room_number"), rs.getString("room_type"),
                rs.getBigDecimal("price_per_night"), rs.getInt("capacity"),
                rs.getBoolean("available"), rs.getString("description"));
    }

    // ── Métodos únicos de Room ──────────────────────────────────────────────
    @Override
    public Optional<Room> findByRoomNumber(String number) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_BY_NUM)) {
            ps.setString(1, number);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new DatabaseException("Error in findByRoomNumber", e); }
    }

    @Override
    public boolean existsByRoomNumber(String number) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(EXISTS_NUM)) {
            ps.setString(1, number);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DatabaseException("Error in existsByRoomNumber", e); }
    }

    @Override
    public List<Room> findAvailable() {
        List<Room> list = new ArrayList<>();
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_AVAIL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { throw new DatabaseException("Error in findAvailable", e); }
        return list;
    }
}
