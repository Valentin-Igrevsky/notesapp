package org.client.controller;

import org.client.http.httpClient;
import org.client.models.Note;
import org.client.models.User;
import org.client.models.httpPacket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Controller {
    private final httpClient client = new httpClient();
    private final Scanner scanner = new Scanner(System.in);
    private User currentUser = null;
    private List<Note> currentNotes = null;

    public void run() {
        while (true) {
            System.out.println("\nГлавное меню:");
            System.out.println("1) Зарегистрироваться");
            System.out.println("2) Войти в систему");
            System.out.println("3) Завершить программу");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Выход...");
                    return;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void register() {
        while (true) {
            System.out.println("\nРегистрация:");
            System.out.print("Имя: ");
            String name = scanner.nextLine();
            System.out.print("Фамилия: ");
            String surname = scanner.nextLine();
            System.out.print("Имя пользователя: ");
            String username = scanner.nextLine();
            System.out.print("Пароль: ");
            String password = scanner.nextLine();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Имя пользователя и пароль обязательны.");
                continue;
            }

            User user = new User(name, surname, username, password);
            try {
                httpPacket response = client.register(user);
                if (response.isCorrect()) {
                    System.out.println("Успешная регистрация.");
                    login();
                    return;
                } else {
                    System.out.println("Ошибка регистрации: " + response.getBody());
                }
            } catch (Exception e) {
                System.out.println("Ошибка подключения: " + e.getMessage());
            }

            System.out.println("1) Повторить\n2) Назад");
            if (scanner.nextLine().equals("2")) return;
        }
    }

    private void login() {
        while (true) {
            System.out.println("\nВход в систему:");
            System.out.print("Имя пользователя: ");
            String username = scanner.nextLine();
            System.out.print("Пароль: ");
            String password = scanner.nextLine();

            try {
                httpPacket response = client.login(username, password);
                if (response.isCorrect()) {
                    currentUser = response.getUser();
                    System.out.println("Вход выполнен.");
                    mainMenu();
                    return;
                } else {
                    System.out.println("Ошибка входа: " + response.getBody());
                }
            } catch (Exception e) {
                System.out.println("Ошибка подключения: " + e.getMessage());
            }

            System.out.println("1) Повторить\n2) Назад");
            if (scanner.nextLine().equals("2")) return;
        }
    }

    private void loadNotesFromServer() {
        try {
            httpPacket response = client.getAllNotes(currentUser.getId());
            if (response.isCorrect()) {
                currentNotes = response.getNotes();
            } else {
                System.out.println("Ошибка при загрузке заметок: " + response.getBody());
                currentNotes = new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("Произошла ошибка при обращении к серверу: " + e.getMessage());
            currentNotes = new ArrayList<>();
        }
    }


    private void mainMenu() {
        loadNotesFromServer();

        while (true) {
            System.out.println("\nГлавное меню:");
            System.out.println("1) Вывести все заметки");
            System.out.println("2) Посмотреть заметку по её номеру");
            System.out.println("3) Создать новую заметку");
            System.out.println("4) Удалить заметку по её номеру");
            System.out.println("5) Отредактировать заметку по её номеру");
            System.out.println("6) Выйти из аккаунта");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    showNotes();
                    break;
                case "2":
                    viewNoteDetails();
                    break;
                case "3":
                    createNote();
                    break;
                case "4":
                    deleteNote();
                    break;
                case "5":
                    editNote();
                    break;
                case "6":
                    System.out.println("Выход из аккаунта...");
                    currentUser = null;
                    currentNotes = null;
                    return;
                default:
                    System.out.println("Неверный ввод. Пожалуйста, выберите пункт от 1 до 6.");
            }
        }
    }


    private void showNotes() {
        try {
            httpPacket response = client.getAllNotes(currentUser.getId());
            if (response.isCorrect()) {
                currentNotes = response.getNotes();
                if (currentNotes.isEmpty()) {
                    System.out.println("Заметок нет.");
                    return;
                }
                int index = 1;
                for (Note note : currentNotes) {
                    System.out.printf("%d) %s%n", index++, note.getTitle());
                }
            } else {
                System.out.println("Ошибка: " + response.getBody());
            }
        } catch (Exception e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }

    private void createNote() {
        String title = null;
        String text = null;

        while (true) {
            System.out.println("\nСоздание новой заметки:");
            System.out.println("1) Ввести заголовок");
            System.out.println("2) Ввести текст");
            System.out.println("3) Вернуться в меню");
            System.out.println("4) Сохранить");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    System.out.print("Заголовок: ");
                    title = scanner.nextLine();
                }
                case "2" -> {
                    System.out.print("Текст: ");
                    text = scanner.nextLine();
                }
                case "3" -> {
                    return;
                }
                case "4" -> {
                    if (title == null || text == null) {
                        System.out.println("Заполните заголовок и текст.");
                    } else {
                        try {
                            Note newNote = new Note(currentUser, text, title);
                            httpPacket response = client.addNote(newNote);
                            if (response.isCorrect()) {
                                System.out.println("Заметка сохранена.");
                                return;
                            } else {
                                System.out.println("Ошибка: " + response.getBody());
                            }
                        } catch (Exception e) {
                            System.out.println("Ошибка подключения: " + e.getMessage());
                        }
                    }
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void deleteNote() {
        showNotes();
        if (currentNotes == null || currentNotes.isEmpty()) return;

        while (true) {
            System.out.println("Введите номер заметки для удаления или 0 для возврата:");
            String input = scanner.nextLine();
            try {
                int num = Integer.parseInt(input);
                if (num == 0) return;

                if (num < 1 || num > currentNotes.size()) {
                    System.out.println("Неверный номер.");
                    continue;
                }

                Note toDelete = currentNotes.get(num - 1);
                System.out.printf("Удалить '%s'? (1 - Да, 2 - Нет)%n", toDelete.getTitle());
                String confirm = scanner.nextLine();

                if (confirm.equals("1")) {
                    httpPacket response = client.deleteNoteById(currentUser.getId(), toDelete.getId());
                    if (response.isCorrect()) {
                        System.out.println("Заметка удалена.");
                        return;
                    } else {
                        System.out.println("Ошибка: " + response.getBody());
                    }
                } else {
                    System.out.println("Удаление отменено.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Введите корректный номер.");
            } catch (Exception e) {
                System.out.println("Ошибка подключения: " + e.getMessage());
            }
        }
    }

    private void editNote() {
        showNotes();
        if (currentNotes == null || currentNotes.isEmpty()) return;

        System.out.println("Введите номер заметки для редактирования или 0 для возврата:");
        String input = scanner.nextLine();

        try {
            int num = Integer.parseInt(input);
            if (num == 0) return;

            if (num < 1 || num > currentNotes.size()) {
                System.out.println("Неверный номер.");
                return;
            }

            Note original = currentNotes.get(num - 1);
            Note edited = new Note(
                    original.getId(),
                    original.getCreateDate(),
                    original.getLastUpdateDate(),
                    original.getOwner(),
                    original.getText(),
                    original.getTitle()
            );

            while (true) {
                System.out.println("\nРедактирование заметки:");
                System.out.println("1) Изменить заголовок");
                System.out.println("2) Изменить текст");
                System.out.println("3) Отмена");
                System.out.println("4) Сохранить");

                String choice = scanner.nextLine();
                switch (choice) {
                    case "1" -> {
                        System.out.print("Новый заголовок: ");
                        edited.setTitle(scanner.nextLine());
                    }
                    case "2" -> {
                        System.out.print("Новый текст: ");
                        edited.setText(scanner.nextLine());
                    }
                    case "3" -> {
                        System.out.println("Редактирование отменено.");
                        return;
                    }
                    case "4" -> {
                        try {
                            httpPacket response = client.patchNote(edited);
                            if (response.isCorrect()) {
                                System.out.println("Заметка обновлена.");
                                return;
                            } else {
                                System.out.println("Ошибка: " + response.getBody());
                            }
                        } catch (Exception e) {
                            System.out.println("Ошибка подключения: " + e.getMessage());
                        }
                    }
                    default -> System.out.println("Неверный выбор.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Введите корректный номер.");
        }
    }

    private void viewNoteDetails() {
        httpPacket response;
        try {
            response = client.getAllNotes(currentUser.getId());
        } catch (Exception e) {
            System.out.println("Ошибка при получении заметок: " + e.getMessage());
            return;
        }

        List<Note> notes = response.getNotes();
        if (notes == null || notes.isEmpty()) {
            System.out.println("У вас нет заметок.");
            return;
        }

        for (int i = 0; i < notes.size(); i++) {
            System.out.println((i + 1) + ") " + notes.get(i).getTitle());
        }

        System.out.print("Введите номер заметки для просмотра или 0 для возврата: ");
        int number = readNumber(notes.size());
        if (number == 0) return;

        Note note = notes.get(number - 1);
        System.out.println("\n====== Заметка ======");
        System.out.println("Заголовок: " + note.getTitle());
        System.out.println("Содержимое: " + note.getText());
        System.out.println("Создана: " + new Date(note.getCreateDate()));
        System.out.println("Обновлена: " + new Date(note.getLastUpdateDate()));
        System.out.println("=====================");
    }

    private int readNumber(int max) {
        while (true) {
            String input = scanner.nextLine();
            try {
                int number = Integer.parseInt(input);
                if (number >= 0 && number <= max) {
                    return number;
                } else {
                    System.out.print("Пожалуйста, введите число от 0 до " + max + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Неверный ввод. Введите число: ");
            }
        }
    }
}
