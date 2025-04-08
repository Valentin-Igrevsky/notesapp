package org.client.screens;

import org.client.http.AuthClient;
import org.client.models.User;
import java.util.Scanner;

public class AuthScreen {
    private final AuthClient authClient = new AuthClient();
    private final Scanner scanner = new Scanner(System.in);

    public Integer show() {
        while (true) {
            System.out.println("\n=== Меню авторизации ===");
            System.out.println("1. Вход");
            System.out.println("2. Регистрация");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    Integer userId = handleLogin();
                    if (userId != null) return userId;
                }
                case 2 -> handleRegister();
                case 0 -> System.exit(0);
                default -> System.out.println("Неверный ввод!");
            }
        }
    }

    private Integer handleLogin() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        try {
            User user = new User();
            user.setLogin(login);
            user.setPassword(password);

            Integer userId = authClient.login(user);
            if (userId != null) {
                System.out.println("Успешный вход! ID: " + userId);
                return userId;
            }
            System.out.println("Ошибка входа!");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        return null;
    }

    private void handleRegister() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        try {
            User user = new User();
            user.setLogin(login);
            user.setPassword(password);

            if (authClient.register(user)) {
                System.out.println("Регистрация успешна!");
            } else {
                System.out.println("Ошибка регистрации");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}