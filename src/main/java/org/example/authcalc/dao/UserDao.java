package org.example.authcalc.dao;

import org.example.authcalc.db.Database;
import org.example.authcalc.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDao {

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setSalt(rs.getString("salt"));
                u.setRole(rs.getString("role"));
                u.setFailedAttempts(rs.getInt("failed_attempts"));
                u.setLockedUntil(rs.getLong("locked_until"));
                return Optional.of(u);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public void save(User user) {
        String sql = "INSERT INTO users(username, password_hash, salt, role, failed_attempts, locked_until) VALUES (?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getRole());
            ps.setInt(5, user.getFailedAttempts());
            ps.setLong(6, user.getLockedUntil());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) user.setId(rs.getLong(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateFailedAttemptsAndLock(User user) {
        String sql = "UPDATE users SET failed_attempts=?, locked_until=? WHERE username=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, user.getFailedAttempts());
            ps.setLong(2, user.getLockedUntil());
            ps.setString(3, user.getUsername());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLockedUntil(0);
        updateFailedAttemptsAndLock(user);
    }
}
