package org.example.authcalc;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.authcalc.dao.UserDao;
import org.example.authcalc.db.Database;
import org.example.authcalc.service.AuthService;
import org.example.authcalc.ui.LoginView;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Database.init();

        String pepper = System.getenv("APP_PEPPER");
        AuthService authService = new AuthService(new UserDao(), pepper);


        if (!new UserDao().findByUsername("user").isPresent()) {
            authService.register("user", "1234".toCharArray(), "USER");
            System.out.println("Создан тестовый пользователь user/1234");
        }

        if (!new UserDao().findByUsername("admin").isPresent()) {
            authService.register("admin", "admin".toCharArray(), "ADMIN");
            System.out.println("Создан тестовый администратор admin/admin");
        }

        LoginView login = new LoginView(authService, primaryStage);
        primaryStage.setTitle("Вход");
        primaryStage.setScene(login.createScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}