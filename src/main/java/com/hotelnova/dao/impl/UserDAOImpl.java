package com.hotelnova.dao.impl;

import com.hotelnova.dao.UserDAO;
import com.hotelnova.exception.DatabaseException;
import com.hotelnova.model.entity.User;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl extends GenericDAOImpl<User, Integer> implements UserDAO {

    private static final String INSERT       = "INSERT INTO users(username,password,full_name,role,active) VALUES(?,?,?,?,?) RETURNING id";
    private static final String UPDATE       = "UPDATE users SET username=?,password=?,full_name=?,role=?,active=? WHERE id=?";
    private static final String DELETE       = "DELETE FROM users WHERE id=?";
    private static final String FIND_BY_ID   = "SELECT * FROM users WHERE id=?";
    private static final String FIND_ALL     = "SELECT * FROM users ORDER BY username";
    private static final String FIND_BY_USER = "SELECT * FROM users WHERE username=?";
    private static final String EXISTS_USER  = "SELECT 1 FROM users WHERE username=? LIMIT 1";

    @Override protected String getInsertSQL()    { return INSERT; }
    @Override protected String getUpdateSQL()    { return UPDATE; }
    @Override protected String getDeleteSQL()    { return DELETE; }
    @Override protected String getFindByIdSQL()  { return FIND_BY_ID; }
    @Override protected String getFindAllSQL()   { return FIND_ALL; }

    @Override
    protected void setInsertParams(PreparedStatement ps, User u) throws SQLException {
        ps.setString(1, u.getUsername()); ps.setString(2, u.getPassword());
        ps.setString(3, u.getFullName()); ps.setString(4, u.getRole());
        ps.setBoolean(5, u.isActive());
    }

    @Override
    protected void setUpdateParams(PreparedStatement ps, User u) throws SQLException {
        ps.setString(1, u.getUsername()); ps.setString(2, u.getPassword());
        ps.setString(3, u.getFullName()); ps.setString(4, u.getRole());
        ps.setBoolean(5, u.isActive());   ps.setInt(6, u.getId());
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
    protected void setGeneratedId(User u, ResultSet rs) throws SQLException {
        u.setId(rs.getInt("id"));
    }

    @Override
    protected User mapRow(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"),
                rs.getString("password"), rs.getString("full_name"),
                rs.getString("role"), rs.getBoolean("active"));
    }

    // ── Métodos únicos de User ──────────────────────────────────────────────
    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_BY_USER)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new DatabaseException("Error in findByUsername", e); }
    }

    @Override
    public boolean existsByUsername(String username) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(EXISTS_USER)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DatabaseException("Error in existsByUsername", e); }
    }
}
