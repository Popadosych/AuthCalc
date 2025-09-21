package org.example.authcalc.service;

import org.example.authcalc.dao.UserDao;
import org.example.authcalc.model.User;
import org.example.authcalc.util.PasswordUtils;

import java.time.Instant;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao;
    private final int MAX_ATTEMPTS = 3;
    private final long LOCK_DURATION_MS = 15 * 60 * 1000L; // 15 минут
    private final String pepper;

    public AuthService(UserDao userDao, String pepper) {
        this.userDao = userDao;
        this.pepper = pepper == null ? "" : pepper;
    }

    public boolean register(String username, char[] password, String role) {
        if (userDao.findByUsername(username).isPresent()) return false;
        String saltBase64 = PasswordUtils.generateSaltBase64();
        byte[] salt = java.util.Base64.getDecoder().decode(saltBase64);
        char[] pwdPlusPepper = combinePasswordPepper(password, pepper);
        String hash = PasswordUtils.hashPassword(pwdPlusPepper, salt);
        java.util.Arrays.fill(pwdPlusPepper, '\0');

        User u = new User();
        u.setUsername(username);
        u.setSalt(saltBase64);
        u.setPasswordHash(hash);
        u.setRole(role == null ? "USER" : role);
        u.setFailedAttempts(0);
        u.setLockedUntil(0);
        userDao.save(u);
        return true;
    }

    public boolean login(String username, char[] password) {
        Optional<User> opt = userDao.findByUsername(username);
        if (!opt.isPresent()) return false;

        User u = opt.get();
        long now = Instant.now().toEpochMilli();
        if (u.getLockedUntil() > now) {
            return false;
        }

        byte[] salt = java.util.Base64.getDecoder().decode(u.getSalt());
        char[] pwdPlusPepper = combinePasswordPepper(password, pepper);
        boolean ok = PasswordUtils.verifyPassword(pwdPlusPepper, salt, u.getPasswordHash());
        java.util.Arrays.fill(pwdPlusPepper, '\0');

        if (ok) {
            userDao.resetFailedAttempts(u);
            return true;
        } else {
            int attempts = u.getFailedAttempts() + 1;
            u.setFailedAttempts(attempts);
            if (attempts >= MAX_ATTEMPTS) {
                u.setLockedUntil(now + LOCK_DURATION_MS);
            }
            userDao.updateFailedAttemptsAndLock(u);
            return false;
        }
    }

    private char[] combinePasswordPepper(char[] password, String pepper) {
        if (pepper == null || pepper.isEmpty()) {
            return password.clone();
        }
        char[] pepperChars = pepper.toCharArray();
        char[] out = new char[password.length + pepperChars.length];
        System.arraycopy(password, 0, out, 0, password.length);
        System.arraycopy(pepperChars, 0, out, password.length, pepperChars.length);
        return out;
    }
}
