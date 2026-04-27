package com.hotelnova.dao.impl;

import com.hotelnova.dao.GenericDAO;
import com.hotelnova.db.ConnectionManager;
import com.hotelnova.exception.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class GenericDAOImpl<T, ID> implements GenericDAO<T, ID> {

    protected final ConnectionManager cm = ConnectionManager.getInstance();

    protected abstract T      mapRow(ResultSet rs)                            throws SQLException;
    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
    protected abstract String getDeleteSQL();
    protected abstract String getFindByIdSQL();
    protected abstract String getFindAllSQL();
    protected abstract void   setInsertParams(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void   setUpdateParams(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void   setDeleteParam(PreparedStatement ps, ID id)     throws SQLException;
    protected abstract void   setFindByIdParam(PreparedStatement ps, ID id)   throws SQLException;
    protected abstract void   setGeneratedId(T entity, ResultSet rs)          throws SQLException;

    @Override
    public T save(T entity) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(getInsertSQL())) {
            setInsertParams(ps, entity);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) setGeneratedId(entity, rs);
            }
        } catch (SQLException e) { throw new DatabaseException("Error in save", e); }
        return entity;
    }

    @Override
    public boolean update(T entity) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(getUpdateSQL())) {
            setUpdateParams(ps, entity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DatabaseException("Error in update", e); }
    }

    @Override
    public boolean deleteById(ID id) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(getDeleteSQL())) {
            setDeleteParam(ps, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DatabaseException("Error in deleteById", e); }
    }

    @Override
    public Optional<T> findById(ID id) {
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(getFindByIdSQL())) {
            setFindByIdParam(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new DatabaseException("Error in findById", e); }
    }

    @Override
    public List<T> findAll() {
        List<T> list = new ArrayList<>();
        try (Connection c = cm.getConnection();
             PreparedStatement ps = c.prepareStatement(getFindAllSQL());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { throw new DatabaseException("Error in findAll", e); }
        return list;
    }
}
