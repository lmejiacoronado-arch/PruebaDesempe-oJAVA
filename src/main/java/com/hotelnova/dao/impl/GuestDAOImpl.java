package com.hotelnova.dao.impl;

import com.hotelnova.dao.GuestDAO;
import com.hotelnova.exception.DatabaseException;
import com.hotelnova.model.entity.Guest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuestDAOImpl extends GenericDAOImpl<Guest, Integer> implements GuestDAO {

    private static final String INSERT      = "INSERT INTO guests(document_id,full_name,email,phone,active) VALUES(?,?,?,?,?) RETURNING id";
    private static final String UPDATE      = "UPDATE guests SET document_id=?,full_name=?,email=?,phone=?,active=? WHERE id=?";
    private static final String DELETE      = "DELETE FROM guests WHERE id=?";
    private static final String FIND_BY_ID  = "SELECT * FROM guests WHERE id=?";
    private static final String FIND_ALL    = "SELECT * FROM guests ORDER BY full_name";
    private static final String FIND_BY_DOC = "SELECT * FROM guests WHERE document_id=?";
    private static final String EXISTS_DOC  = "SELECT 1 FROM guests WHERE document_id=? LIMIT 1";
    private static final String FIND_BY_NAME= "SELECT * FROM guests WHERE full_name ILIKE ?";

    @Override protected String getInsertSQL()    { return INSERT; }
    @Override protected String getUpdateSQL()    { return UPDATE; }
    @Override protected String getDeleteSQL()    { return DELETE; }
    @Override protected String getFindByIdSQL()  { return FIND_BY_ID; }
    @Override protected String getFindAllSQL()   { return FIND_ALL; }

    @Override
    protected void setInsertParams(PreparedStatement ps, Guest g) throws SQLException {
        ps.setString(1, g.getDocumentId()); ps.setString(2, g.getFullName());
        ps.setString(3, g.getEmail());      ps.setString(4, g.getPhone());
        ps.setBoolean(5, g.isActive());
    }

    @Override
    protected void setUpdateParams(PreparedStatement ps, Guest g) throws SQLException {
        ps.setString(1, g.getDocumentId()); ps.setString(2, g.getFullName());
        ps.setString(3, g.getEmail());      ps.setString(4, g.getPhone());
        ps.setBoolean(5, g.isActive());     ps.setInt(6, g.getId());
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
    protected void setGeneratedId(Guest g, ResultSet rs) throws SQLException {
        g.setId(rs.getInt("id"));
    }

    @Override
    protected Guest mapRow(ResultSet rs) throws SQLException {
        return new Guest(rs.getInt("id"), rs.getString("document_id"),
                rs.getString("full_name"), rs.getString("email"),
                rs.getString("phone"), rs.getBoolean("active"));
    }

    // ── Métodos únicos de Guest ─────────────────────────────────────────────
    @Override
    public Optional<Guest> findByDocumentId(String documentId) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_BY_DOC)) {
            ps.setString(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new DatabaseException("Error in findByDocumentId", e); }
    }

    @Override
    public boolean existsByDocumentId(String documentId) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(EXISTS_DOC)) {
            ps.setString(1, documentId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DatabaseException("Error in existsByDocumentId", e); }
    }

    @Override
    public List<Guest> findByName(String name) {
        List<Guest> list = new ArrayList<>();
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_BY_NAME)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        } catch (SQLException e) { throw new DatabaseException("Error in findByName", e); }
        return list;
    }
}
