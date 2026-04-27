package com.hotelnova.util;

import java.io.*;
import java.util.logging.*;

public class AppLogger {
    private static AppLogger instance;
    private final Logger logger;

    private AppLogger() {
        logger = Logger.getLogger("HotelNova");
        try {
            new File("logs").mkdirs();
            FileHandler fh = new FileHandler("logs/app.log", 2_000_000, 3, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(true);
        } catch (IOException e) {
            logger.warning("Could not init log file: " + e.getMessage());
        }
    }

    public static AppLogger getInstance() {
        if (instance == null) { synchronized (AppLogger.class) {
            if (instance == null) instance = new AppLogger(); } }
        return instance;
    }

    public void info(String msg)  { logger.info(msg); }
    public void warn(String msg)  { logger.warning(msg); }
    public void error(String msg) { logger.severe(msg); }
}
