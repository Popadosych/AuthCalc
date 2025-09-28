package org.example.authcalc.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.authcalc.service.AuthService;

public class RegisterView {
    private final AuthService authService;
    private final Stage stage;

    public RegisterView(AuthService authService, Stage stage) {
        this.authService = authService;
        this.stage = stage;
    }

    public Scene createScene() {
        Label usernameLabel = new Label("Логин:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Пароль:");
        PasswordField passwordField = new PasswordField();

        Label confirmLabel = new Label("Подтверждение пароля:");
        PasswordField confirmField = new PasswordField();

        Button registerButton = new Button("Создать аккаунт");
        Button backButton = new Button("Назад");

        Label statusLabel = new Label();

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (username.isBlank() || password.isBlank()) {
                statusLabel.setText("Введите логин и пароль!");
                return;
            }
            if (!password.equals(confirm)) {
                statusLabel.setText("Пароли не совпадают!");
                return;
            }

            try {
                authService.register(username, password.toCharArray(), "USER");
                statusLabel.setText("Аккаунт создан! Теперь войдите.");
            } catch (Exception ex) {
                statusLabel.setText("Ошибка: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> {
            LoginView loginView = new LoginView(authService, stage);
            stage.setScene(loginView.createScene());
        });

        VBox vbox = new VBox(10, usernameLabel, usernameField,
                passwordLabel, passwordField,
                confirmLabel, confirmField,
                registerButton, backButton, statusLabel);
        vbox.setPadding(new Insets(20));

        return new Scene(vbox, 350, 300);
    }
}