package org.example.authcalc.db;

import java.sql.*;

public class Database {
    private static final String URL = "jdbc:sqlite:authcalc.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "salt TEXT NOT NULL," +
                "role TEXT DEFAULT 'USER'," +
                "failed_attempts INTEGER DEFAULT 0," +
                "locked_until INTEGER DEFAULT 0" +
                ");";
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
