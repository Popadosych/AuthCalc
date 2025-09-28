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
        String createTableSql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "salt TEXT NOT NULL," +
                "role TEXT DEFAULT 'USER'," +
                "global_lock_until INTEGER DEFAULT 0" +
                ");";

        String addColumnSql = "ALTER TABLE users ADD COLUMN global_lock_until INTEGER DEFAULT 0;";

        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute(createTableSql);

            // Проверяем, существует ли столбец 'global_lock_until'
            DatabaseMetaData metaData = c.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "users", "global_lock_until");
            if (!columns.next()) {
                // Если столбец не найден, добавляем его
                st.execute(addColumnSql);
                System.out.println("Столбец 'global_lock_until' добавлен в таблицу 'users'.");
            }

            // ⚠️ ВАЖНО: Удалите старую базу данных, если она уже содержит
            // failed_attempts и locked_until, иначе возникнет ошибка.
            // Второй вариант - удалить эти столбцы вручную, но это сложнее.
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}