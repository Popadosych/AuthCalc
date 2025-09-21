package org.example.authcalc.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CalculatorView {

    public Scene createScene() {
        TextField a = new TextField();
        TextField b = new TextField();
        ComboBox<String> op = new ComboBox<>();
        op.getItems().addAll("+", "-", "*", "/");
        op.getSelectionModel().selectFirst();
        Button calc = new Button("Вычислить");
        Label result = new Label();

        calc.setOnAction(e -> {
            try {
                double x = Double.parseDouble(a.getText());
                double y = Double.parseDouble(b.getText());
                String o = op.getValue();
                double res;
                switch (o) {
                    case "+": res = x + y; break;
                    case "-": res = x - y; break;
                    case "*": res = x * y; break;
                    case "/": res = x / y; break;
                    default: res = 0; break;
                }
                result.setText(String.valueOf(res));
            } catch (Exception ex) {
                result.setText("Ошибка ввода");
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("A:"), 0, 0);
        grid.add(a, 1, 0);
        grid.add(new Label("B:"), 0, 1);
        grid.add(b, 1, 1);
        grid.add(new Label("Операция:"), 0, 2);
        grid.add(op, 1, 2);
        grid.add(calc, 1, 3);
        grid.add(result, 1, 4);

        VBox root = new VBox(10, grid);
        root.setPadding(new Insets(10));
        return new Scene(root, 360, 260);
    }
}
