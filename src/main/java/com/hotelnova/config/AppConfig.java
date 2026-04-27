package com.hotelnova.config;

import java.io.*;
import java.math.BigDecimal;
import java.util.Properties;

public class AppConfig {
    private static AppConfig instance;
    private final Properties props = new Properties();

    private AppConfig() {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is == null) throw new RuntimeException("config.properties not found");
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error loading config.properties", e);
        }
    }

    public static AppConfig getInstance() {
        if (instance == null) { synchronized (AppConfig.class) {
            if (instance == null) instance = new AppConfig(); } }
        return instance;
    }

    public String getDbUrl()       { return props.getProperty("db.url"); }
    public String getDbUser()      { return props.getProperty("db.user"); }
    public String getDbPassword()  { return props.getProperty("db.password"); }
    public String getDbDriver()    { return props.getProperty("db.driver"); }
    public String getAppName()     { return props.getProperty("app.name", "HotelNova"); }
    public String getViewType()    { return props.getProperty("view.type", "swing"); }
    public String getReportDir()   { return props.getProperty("report.dir", "reports/"); }
    public BigDecimal getTaxRate() {
        return new BigDecimal(props.getProperty("hotel.tax.rate", "0.19"));
    }
}
