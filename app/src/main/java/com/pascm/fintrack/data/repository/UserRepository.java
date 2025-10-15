package com.pascm.fintrack.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.UserDao;
import com.pascm.fintrack.data.local.entity.User;
import com.pascm.fintrack.data.local.entity.UserProfile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Repository para gestión de usuarios y autenticación local (sin Firebase).
 */
public class UserRepository {

    private final UserDao userDao;
    private final FinTrackDatabase database;

    public UserRepository(Context context) {
        this.database = FinTrackDatabase.getDatabase(context);
        this.userDao = database.userDao();
    }

    // ========== Autenticación ==========

    public void registerUser(String email, String password, AuthCallback callback) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                if (!isValidEmail(email)) {
                    callback.onResult(AuthResult.error("Email inválido"));
                    return;
                }
                if (password == null || password.length() < 6) {
                    callback.onResult(AuthResult.error("La contraseña debe tener al menos 6 caracteres"));
                    return;
                }

                // ¿Existe ya?
                User existing = userDao.getByEmail(email);
                if (existing != null) {
                    callback.onResult(AuthResult.error("Este email ya está registrado"));
                    return;
                }

                // Crear usuario
                User user = new User();
                user.setEmail(email);
                user.setPasswordHash(hashPassword(password));
                user.setStatus(User.UserStatus.ACTIVE);
                user.setCreatedAt(Instant.now());
                user.setUpdatedAt(Instant.now());
                user.setLastLoginAt(Instant.now());

                long userId = userDao.insert(user);
                user.setUserId(userId);

                // Crear perfil por defecto
                UserProfile profile = new UserProfile();
                profile.setUserId(userId);
                profile.setFullName(extractNameFromEmail(email));
                profile.setLanguage("es");
                profile.setDefaultCurrency("MXN");
                profile.setTheme(UserProfile.Theme.LIGHT);
                profile.setUpdatedAt(Instant.now());
                userDao.insertProfile(profile);

                android.util.Log.i("UserRepository", "Usuario registrado: " + email + " (ID: " + userId + ")");
                callback.onResult(AuthResult.success(user));

            } catch (Exception e) {
                android.util.Log.e("UserRepository", "Error al registrar", e);
                callback.onResult(AuthResult.error("Error al registrar: " + e.getMessage()));
            }
        });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                User user = userDao.getByEmail(email);
                if (user == null) {
                    callback.onResult(AuthResult.error("Email o contraseña incorrectos"));
                    return;
                }
                if (user.getStatus() != User.UserStatus.ACTIVE) {
                    callback.onResult(AuthResult.error("Esta cuenta está " + user.getStatus().name().toLowerCase()));
                    return;
                }
                String passwordHash = hashPassword(password);
                if (user.getPasswordHash() == null || !passwordHash.equals(user.getPasswordHash())) {
                    callback.onResult(AuthResult.error("Email o contraseña incorrectos"));
                    return;
                }

                user.setLastLoginAt(Instant.now());
                user.setUpdatedAt(Instant.now());
                userDao.update(user);

                android.util.Log.i("UserRepository", "Login: " + email + " (ID: " + user.getUserId() + ")");
                callback.onResult(AuthResult.success(user));

            } catch (Exception e) {
                android.util.Log.e("UserRepository", "Error de login", e);
                callback.onResult(AuthResult.error("Error al iniciar sesión: " + e.getMessage()));
            }
        });
    }

    public void changePassword(long userId, String currentPassword, String newPassword, AuthCallback callback) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                User user = userDao.getByIdSync(userId);
                if (user == null) {
                    callback.onResult(AuthResult.error("Usuario no encontrado"));
                    return;
                }
                String currentHash = hashPassword(currentPassword);
                if (user.getPasswordHash() == null || !currentHash.equals(user.getPasswordHash())) {
                    callback.onResult(AuthResult.error("Contraseña actual incorrecta"));
                    return;
                }
                if (newPassword == null || newPassword.length() < 6) {
                    callback.onResult(AuthResult.error("La nueva contraseña debe tener al menos 6 caracteres"));
                    return;
                }
                String newHash = hashPassword(newPassword);
                userDao.updatePasswordHash(userId, newHash, Instant.now().toEpochMilli());
                callback.onResult(AuthResult.success(user));
            } catch (Exception e) {
                callback.onResult(AuthResult.error("Error al cambiar contraseña: " + e.getMessage()));
            }
        });
    }

    // ========== Consultas ==========

    public LiveData<User> getUserById(long userId) {
        return userDao.getById(userId);
    }

    public User getUserByIdSync(long userId) {
        return userDao.getByIdSync(userId);
    }

    public LiveData<User> getUserByEmail(String email) {
        return userDao.getByEmailLive(email);
    }

    public LiveData<UserProfile> getUserProfile(long userId) {
        return userDao.getProfile(userId);
    }

    public void updateUserProfile(UserProfile profile) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            profile.setUpdatedAt(Instant.now());
            userDao.updateProfile(profile);
        });
    }

    public void updateUserStatus(long userId, User.UserStatus status) {
        FinTrackDatabase.databaseWriteExecutor.execute(() ->
                userDao.updateStatus(userId, status.name(), Instant.now().toEpochMilli())
        );
    }

    // ========== Helpers ==========

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((password == null ? "" : password).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private String extractNameFromEmail(String email) {
        if (email == null) return "Usuario";
        String local = email.split("@")[0];
        if (local.isEmpty()) return "Usuario";
        return Character.toUpperCase(local.charAt(0)) + local.substring(1);
    }

    // ========== Resultados de Auth ==========

    public interface AuthCallback {
        void onResult(AuthResult result);
    }

    public static class AuthResult {
        private final boolean success;
        private final User user;
        private final String errorMessage;

        private AuthResult(boolean success, User user, String errorMessage) {
            this.success = success;
            this.user = user;
            this.errorMessage = errorMessage;
        }

        public static AuthResult success(User user) {
            return new AuthResult(true, user, null);
        }

        public static AuthResult error(String message) {
            return new AuthResult(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public User getUser() { return user; }
        public String getErrorMessage() { return errorMessage; }
    }
}
