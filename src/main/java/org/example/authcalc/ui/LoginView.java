package org.example.authcalc.ui;

import org.example.authcalc.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {
    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;
    }

    public Scene createScene(Stage primaryStage) {
        Label userLabel = new Label("Логин:");
        TextField userField = new TextField();

        Label passLabel = new Label("Пароль:");
        PasswordField passField = new PasswordField();

        Button loginBtn = new Button("Войти");
        Label info = new Label();

        loginBtn.setOnAction(e -> {
            String user = userField.getText();
            char[] pass = passField.getText().toCharArray();
            boolean ok = authService.login(user, pass);
            java.util.Arrays.fill(pass, '\0'); // очистить временный массив
            if (ok) {
                CalculatorView calc = new CalculatorView();
                primaryStage.setScene(calc.createScene());
                primaryStage.setTitle("Калькулятор");
            } else {
                // можно уточнить причину — но здесь простой текст
                info.setText("Неверные данные или аккаунт заблокирован.");
            }
        });

        VBox box = new VBox(8, userLabel, userField, passLabel, passField, loginBtn, info);
        box.setPadding(new Insets(15));
        return new Scene(box, 360, 260);
    }
}
