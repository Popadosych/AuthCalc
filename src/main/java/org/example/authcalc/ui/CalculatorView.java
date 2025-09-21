package org.example.authcalc.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.authcalc.service.AuthService;

public class CalculatorView {
    private final Stage stage;
    private final AuthService authService;

    public CalculatorView(Stage stage, AuthService authService) {
        this.stage = stage;
        this.authService = authService;
    }

    public void show() {
        // Получаем имя текущего пользователя
        String username = authService.getCurrentUser().getUsername();
        Label userLabel = new Label("Пользователь: " + username);

        Label label = new Label("Калькулятор");
        TextField input1 = new TextField();
        TextField input2 = new TextField();
        ComboBox<String> op = new ComboBox<>();
        op.getItems().addAll("+", "-", "*", "/");
        op.getSelectionModel().selectFirst(); // выбираем по умолчанию "+"
        Label result = new Label();

        Button calcButton = new Button("Вычислить");
        Button logoutButton = new Button("Выйти");

        calcButton.setOnAction(e -> {
            try {
                double a = Double.parseDouble(input1.getText());
                double b = Double.parseDouble(input2.getText());
                String operation = op.getValue();
                double res;

                switch (operation) {
                    case "+": res = a + b; break;
                    case "-": res = a - b; break;
                    case "*": res = a * b; break;
                    case "/": res = a / b; break;
                    default: res = 0; break;
                }

                result.setText("Результат: " + res);
            } catch (NumberFormatException ex) {
                result.setText("Ошибка: введите числа");
            }
        });

        logoutButton.setOnAction(e -> {
            authService.logout();
            LoginView loginView = new LoginView(authService, stage);
            stage.setScene(loginView.createScene());
        });

        VBox vbox = new VBox(10, userLabel, label, input1, input2, op, calcButton, result, logoutButton);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 350, 320);
        stage.setScene(scene);
    }
}
