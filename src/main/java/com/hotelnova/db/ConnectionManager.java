package com.hotelnova.db;

import com.hotelnova.config.AppConfig;
import java.sql.*;

public class ConnectionManager {
    private static ConnectionManager instance;
    private final AppConfig config = AppConfig.getInstance();

    private ConnectionManager() {
        try { Class.forName(config.getDbDriver()); }
        catch (ClassNotFoundException e) { throw new RuntimeException("JDBC driver not found", e); }
    }

    public static ConnectionManager getInstance() {
        if (instance == null) { synchronized (ConnectionManager.class) {
            if (instance == null) instance = new ConnectionManager(); } }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getDbUrl(), config.getDbUser(), config.getDbPassword());
    }
}
