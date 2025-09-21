package org.example.authcalc.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.authcalc.service.AuthService;

public class LoginView {
    private final AuthService authService;
    private final Stage stage;

    public LoginView(AuthService authService, Stage stage) {
        this.authService = authService;
        this.stage = stage;
    }

    public Scene createScene() {
        Label usernameLabel = new Label("Логин:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Пароль:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Войти");
        Button registerButton = new Button("Регистрация");

        Label statusLabel = new Label();

        loginButton.setOnAction(e -> {
            try {
                authService.login(usernameField.getText(), passwordField.getText().toCharArray());
                statusLabel.setText("Успешный вход!");
                new CalculatorView(stage, authService).show();
            } catch (Exception ex) {
                statusLabel.setText("Ошибка: " + ex.getMessage());
            }
        });

        registerButton.setOnAction(e -> {
            RegisterView registerView = new RegisterView(authService, stage);
            stage.setScene(registerView.createScene());
        });

        VBox vbox = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField,
                loginButton, registerButton, statusLabel);
        vbox.setPadding(new Insets(20));

        return new Scene(vbox, 300, 250);
    }
}
