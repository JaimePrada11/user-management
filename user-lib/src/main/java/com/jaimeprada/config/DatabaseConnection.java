package com.jaimeprada.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static volatile DatabaseConnection instance;

    private final String url;
    private final String username;
    private final String password;

    private DatabaseConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static void init(String url, String username, String password ) {
        if (instance != null) {
            return;
        }
        synchronized (DatabaseConnection.class) {
            if (instance == null) {
                instance = new DatabaseConnection(url, username, password);
            }
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null){
            throw new IllegalStateException("DatabaseConnection not initialized");
        }

        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    static void reset() {
        instance = null;
    }
}
