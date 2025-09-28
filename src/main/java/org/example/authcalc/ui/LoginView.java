package org.example.authcalc.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.authcalc.service.AuthService;
import org.example.authcalc.service.AuthService.LoginResult;
import org.example.authcalc.service.AuthService.LoginStatus;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginView {
    private final AuthService authService;
    private final Stage stage;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label statusLabel;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public LoginView(AuthService authService, Stage stage) {
        this.authService = authService;
        this.stage = stage;
    }

    public Scene createScene() {
        Label usernameLabel = new Label("Логин:");
        usernameField = new TextField();

        Label passwordLabel = new Label("Пароль:");
        passwordField = new PasswordField();

        loginButton = new Button("Войти");
        Button registerButton = new Button("Регистрация");

        statusLabel = new Label();

        loginButton.setOnAction(e -> {
            LoginResult result = authService.login(usernameField.getText(), passwordField.getText().toCharArray());
            if (result.getStatus() == LoginStatus.SUCCESS) {
                statusLabel.setText("Успешный вход!");
                new CalculatorView(stage, authService).show();
            } else if (result.getStatus() == LoginStatus.LOCKED) {
                long remainingSeconds = (result.getLockedUntil() - Instant.now().toEpochMilli()) / 1000;
                statusLabel.setText(String.format("Аккаунт заблокирован на %d секунд.", remainingSeconds));
                updateLockStatus();
            } else {
                statusLabel.setText("Неверный логин или пароль!");
            }
        });

        registerButton.setOnAction(e -> {
            RegisterView registerView = new RegisterView(authService, stage);
            stage.setScene(registerView.createScene());
        });

        VBox vbox = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField,
                loginButton, registerButton, statusLabel);
        vbox.setPadding(new Insets(20));

        updateLockStatus();

        return new Scene(vbox, 300, 250);
    }

    private void updateLockStatus() {
        long lockedUntil = authService.getGlobalLockUntil();
        long now = Instant.now().toEpochMilli();
        boolean isLocked = lockedUntil > now;

        usernameField.setDisable(isLocked);
        passwordField.setDisable(isLocked);
        loginButton.setDisable(isLocked);

        if (isLocked) {
            long remainingSeconds = (lockedUntil - now) / 1000;
            statusLabel.setText(String.format("Приложение заблокировано на %d секунд.", remainingSeconds));

            scheduler.schedule(() -> {
                Platform.runLater(() -> {
                    updateLockStatus();
                    if (!authService.isGloballyLocked()) {
                        statusLabel.setText("Приложение разблокировано. Попробуйте снова.");
                    }
                });
            }, remainingSeconds + 1, TimeUnit.SECONDS);

        } else {
            statusLabel.setText("");
        }
    }
}