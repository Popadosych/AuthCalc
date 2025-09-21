package org.example.authcalc;

import org.example.authcalc.dao.UserDao;
import org.example.authcalc.db.Database;
import org.example.authcalc.service.AuthService;
import org.example.authcalc.ui.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Database.init();

        String pepper = System.getenv("APP_PEPPER"); // optional
        AuthService authService = new AuthService(new UserDao(), pepper);

        // создаём тестового пользователя если нет
        if (!new UserDao().findByUsername("user").isPresent()) {
            authService.register("user", "1234".toCharArray(), "USER");
            System.out.println("Создан тестовый пользователь user/1234");
        }

        LoginView login = new LoginView(authService);
        primaryStage.setTitle("Вход");
        primaryStage.setScene(login.createScene(primaryStage));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
