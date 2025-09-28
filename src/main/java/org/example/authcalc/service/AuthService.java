package org.example.authcalc.service;

import org.example.authcalc.dao.UserDao;
import org.example.authcalc.model.User;
import org.example.authcalc.util.PasswordUtils;

import java.time.Instant;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao;
    private final String pepper;
    private User currentUser;

    // Глобальная блокировка
    private final int MAX_GLOBAL_ATTEMPTS = 5;
    private final long GLOBAL_LOCK_DURATION_MS = 60 * 1000L;
    private int globalFailedAttempts = 0;

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
        userDao.save(u);
        return true;
    }

    public LoginResult login(String username, char[] password) {
        if (isGloballyLocked()) {
            return new LoginResult(LoginStatus.LOCKED, getGlobalLockUntil());
        }

        Optional<User> opt = userDao.findByUsername(username);
        if (!opt.isPresent()) {
            handleFailedLogin();
            return LoginResult.INVALID;
        }

        User u = opt.get();

        byte[] salt = java.util.Base64.getDecoder().decode(u.getSalt());
        char[] pwdPlusPepper = combinePasswordPepper(password, pepper);
        boolean ok = PasswordUtils.verifyPassword(pwdPlusPepper, salt, u.getPasswordHash());
        java.util.Arrays.fill(pwdPlusPepper, '\0');

        if (ok) {
            currentUser = u;
            globalFailedAttempts = 0;
            return LoginResult.SUCCESS;
        } else {
            handleFailedLogin();
            return LoginResult.INVALID;
        }
    }

    private void handleFailedLogin() {
        globalFailedAttempts++;
        if (globalFailedAttempts >= MAX_GLOBAL_ATTEMPTS) {
            long lockUntil = Instant.now().toEpochMilli() + GLOBAL_LOCK_DURATION_MS;
            userDao.updateGlobalLock(lockUntil);
            globalFailedAttempts = 0;
        }
    }

    public long getGlobalLockUntil() {
        return userDao.getGlobalLockUntil();
    }

    public boolean isGloballyLocked() {
        return getGlobalLockUntil() > Instant.now().toEpochMilli();
    }

    public void logout() {
        currentUser = null;
    }

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