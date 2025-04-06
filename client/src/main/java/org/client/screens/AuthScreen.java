package org.client.screens;

import org.client.http.AuthClient;
import org.client.models.User;
import java.util.Scanner;

public class AuthScreen {
    private final AuthClient authClient;
    private final Scanner scanner;

    public AuthScreen() {
        this.authClient = new AuthClient();
        this.scanner = new Scanner(System.in);
    }

    public User show() {
        while (true) {
            System.out.println("\n=== Меню авторизации ===");
            System.out.println("1. Вход");
            System.out.println("2. Регистрация");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Очистка буфера

            switch (choice) {
                case 1 -> { return handleLogin(); }
                case 2 -> { handleRegister(); }
                case 0 -> { System.exit(0); }
                default -> System.out.println("Неверный ввод!");
            }
        }
    }

    private User handleLogin() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        try {
            User user = new User(login, password);
            if (authClient.login(user)) {
                System.out.println("Успешный вход!");
                return user;
            } else {
                System.out.println("Ошибка входа!");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        return null;
    }

    private void handleRegister() {
        System.out.print("Придумайте логин: ");
        String login = scanner.nextLine();
        System.out.print("Придумайте пароль: ");
        String password = scanner.nextLine();

        try {
            if (authClient.register(new User(login, password))) {
                System.out.println("Регистрация успешна!");
            } else {
                System.out.println("Ошибка регистрации!");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}