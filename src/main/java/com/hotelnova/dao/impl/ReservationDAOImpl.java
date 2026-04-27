package com.hotelnova.dao.impl;

import com.hotelnova.dao.ReservationDAO;
import com.hotelnova.exception.DatabaseException;
import com.hotelnova.model.entity.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationDAOImpl extends GenericDAOImpl<Reservation, Integer> implements ReservationDAO {

    private static final String SELECT_FULL =
            "SELECT r.*, g.full_name AS guest_name, rm.room_number " +
                    "FROM reservations r " +
                    "JOIN guests g  ON r.guest_id = g.id " +
                    "JOIN rooms  rm ON r.room_id  = rm.id ";

    private static final String INSERT =
            "INSERT INTO reservations(guest_id,room_id,user_id,check_in,check_out,status,total_cost,notes) " +
                    "VALUES(?,?,?,?,?,?,?,?) RETURNING id";
    private static final String UPDATE_SIMPLE =
            "UPDATE reservations SET status=?,total_cost=?,notes=?,check_in=?,check_out=? WHERE id=?";
    private static final String UPDATE_TX =
            "UPDATE reservations SET status=?,total_cost=?,notes=? WHERE id=?";
    private static final String DELETE = "DELETE FROM reservations WHERE id=?";
    private static final String FIND_BY_ID = SELECT_FULL + "WHERE r.id=?";
    private static final String FIND_ALL = SELECT_FULL + "ORDER BY r.check_in DESC";
    private static final String FIND_ACTIVE = SELECT_FULL + "WHERE r.status='ACTIVE' ORDER BY r.check_in";
    private static final String FIND_BY_GUEST = SELECT_FULL + "WHERE r.guest_id=? ORDER BY r.check_in DESC";
    private static final String FIND_BY_ROOM = SELECT_FULL + "WHERE r.room_id=? ORDER BY r.check_in DESC";
    private static final String FIND_ACTIVE_ROOM = SELECT_FULL + "WHERE r.room_id=? AND r.status='ACTIVE' LIMIT 1";
    private static final String HAS_OVERLAP =
            "SELECT 1 FROM reservations " +
                    "WHERE room_id=? AND id<>? AND status='ACTIVE' " +
                    "AND check_in < ? AND check_out > ? LIMIT 1";

    @Override
    protected String getInsertSQL() {
        return INSERT;
    }

    @Override
    protected String getUpdateSQL() {
        return UPDATE_SIMPLE;
    }

    @Override
    protected String getDeleteSQL() {
        return DELETE;
    }

    @Override
    protected String getFindByIdSQL() {
        return FIND_BY_ID;
    }

    @Override
    protected String getFindAllSQL() {
        return FIND_ALL;
    }

    @Override
    protected void setInsertParams(PreparedStatement ps, Reservation r) throws SQLException {
        ps.setInt(1, r.getGuestId());
        ps.setInt(2, r.getRoomId());
        ps.setInt(3, r.getUserId());
        ps.setDate(4, java.sql.Date.valueOf(r.getCheckIn()));
        ps.setDate(5, java.sql.Date.valueOf(r.getCheckOut()));
        ps.setString(6, r.getStatus());
        ps.setBigDecimal(7, r.getTotalCost());
        ps.setString(8, r.getNotes());
    }

    @Override
    protected void setUpdateParams(PreparedStatement ps, Reservation r) throws SQLException {
        ps.setString(1, r.getStatus());
        ps.setBigDecimal(2, r.getTotalCost());
        ps.setString(3, r.getNotes());
        ps.setDate(4, java.sql.Date.valueOf(r.getCheckIn()));
        ps.setDate(5, java.sql.Date.valueOf(r.getCheckOut()));
        ps.setInt(6, r.getId());
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
    protected void setGeneratedId(Reservation r, ResultSet rs) throws SQLException {
        r.setId(rs.getInt("id"));
    }

    @Override
    protected Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation(
                rs.getInt("id"), rs.getInt("guest_id"), rs.getInt("room_id"),
                rs.getInt("user_id"), rs.getDate("check_in").toLocalDate(),
                rs.getDate("check_out").toLocalDate(), rs.getString("status"),
                rs.getBigDecimal("total_cost"), rs.getString("notes"));
        try {
            r.setGuestName(rs.getString("guest_name"));
        } catch (SQLException ignored) {
        }
        try {
            r.setRoomNumber(rs.getString("room_number"));
        } catch (SQLException ignored) {
        }
        return r;
    }

    // ── Sobrescritura transaccional de save/update ──────────────────────────
    @Override
    public Reservation save(Connection conn, Reservation res) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT)) {
            setInsertParams(ps, res);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) setGeneratedId(res, rs);
            }
        }
        return res;
    }

    @Override
    public boolean update(Connection conn, Reservation res) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_TX)) {
            ps.setString(1, res.getStatus());
            ps.setBigDecimal(2, res.getTotalCost());
            ps.setString(3, res.getNotes());
            ps.setInt(4, res.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ── Métodos únicos de Reservation ──────────────────────────────────────
    @Override
    public List<Reservation> findActive() {
        List<Reservation> list = new ArrayList<>();
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_ACTIVE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error in findActive", e);
        }
        return list;
    }

    @Override
    public List<Reservation> findByGuestId(int guestId) {
        List<Reservation> list = new ArrayList<>();
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_BY_GUEST)) {
            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error in findByGuestId", e);
        }
        return list;
    }

    @Override
    public List<Reservation> findByRoomId(int roomId) {
        List<Reservation> list = new ArrayList<>();
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_BY_ROOM)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error in findByRoomId", e);
        }
        return list;
    }

    @Override
    public Optional<Reservation> findActiveByRoomId(int roomId) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_ACTIVE_ROOM)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error in findActiveByRoomId", e);
        }
    }

    @Override
    public boolean hasOverlap(int roomId, LocalDate checkIn, LocalDate checkOut, int excludeId) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(HAS_OVERLAP)) {
            ps.setInt(1, roomId);
            ps.setInt(2, excludeId);
            ps.setDate(3, java.sql.Date.valueOf(checkOut));
            ps.setDate(4, java.sql.Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error in hasOverlap", e);
        }
    }
}
