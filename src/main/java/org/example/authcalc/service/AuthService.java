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
    private User currentUser; // 🔹 добавлено

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

    public LoginResult login(String username, char[] password) {
        Optional<User> opt = userDao.findByUsername(username);
        if (!opt.isPresent()) return LoginResult.INVALID;

        User u = opt.get();
        long now = Instant.now().toEpochMilli();
        if (u.getLockedUntil() > now) {
            return new LoginResult(LoginStatus.LOCKED, u.getLockedUntil());
        }

        byte[] salt = java.util.Base64.getDecoder().decode(u.getSalt());
        char[] pwdPlusPepper = combinePasswordPepper(password, pepper);
        boolean ok = PasswordUtils.verifyPassword(pwdPlusPepper, salt, u.getPasswordHash());
        java.util.Arrays.fill(pwdPlusPepper, '\0');

        if (ok) {
            userDao.resetFailedAttempts(u);
            currentUser = u; // 🔹 сохраняем залогиненного пользователя
            return LoginResult.SUCCESS;
        } else {
            int attempts = u.getFailedAttempts() + 1;
            u.setFailedAttempts(attempts);
            if (attempts >= MAX_ATTEMPTS) {
                u.setLockedUntil(now + LOCK_DURATION_MS);
            }
            userDao.updateFailedAttemptsAndLock(u);
            return LoginResult.INVALID;
        }
    }

    /** 🔹 Разлогинивание */
    public void logout() {
        currentUser = null;
    }

    /** 🔹 Получить текущего пользователя */
    public User getCurrentUser() {
        return currentUser;
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

    public static class LoginResult {
        private final LoginStatus status;
        private final Long lockedUntil;

        public LoginResult(LoginStatus status) {
            this(status, null);
        }

        public LoginResult(LoginStatus status, Long lockedUntil) {
            this.status = status;
            this.lockedUntil = lockedUntil;
        }

        public static final LoginResult SUCCESS = new LoginResult(LoginStatus.SUCCESS);
        public static final LoginResult INVALID = new LoginResult(LoginStatus.INVALID);

        public LoginStatus getStatus() { return status; }
        public Long getLockedUntil() { return lockedUntil; }
    }

    public enum LoginStatus {
        SUCCESS, INVALID, LOCKED
    }
}
