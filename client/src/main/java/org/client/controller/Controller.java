package org.client.controller;

import org.client.http.httpClient;
import org.client.models.Note;
import org.client.models.User;
import org.client.models.httpPacket;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Controller {
    private final httpClient client = new httpClient();
    private final Scanner scanner = new Scanner(System.in);
    private User currentUser;
    private List<Note> localNotes = new ArrayList<>();
    private List<Note> serverNotes = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final File storage = new File("session.json");
    private boolean autoSync = true;

    public Controller() {
        loadFromFile();
    }

    public void run() {
        if (currentUser == null) {
            startMenu();
        } else {
            mainMenu();
        }
    }

    private void startMenu() {
        while (true) {
            System.out.println("\nДобро пожаловать в систему заметок!");
            System.out.println("1) Зарегистрироваться");
            System.out.println("2) Войти в систему");
            System.out.println("3) Завершить программу");
            System.out.print("> ");

            switch (scanner.nextLine()) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> System.exit(0);
                default -> System.out.println("Неверный ввод. Попробуйте снова.");
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
                serverNotes = response.getNotes();
            } else {
                serverNotes = new ArrayList<>();
            }
        } catch (Exception e) {
            serverNotes = new ArrayList<>();
        }
    }


    private void mainMenu() {
        System.out.format("Добро пожаловать в систему заметок, %s\n", currentUser.getName());
        while (true) {
            System.out.println("\nГлавное меню:");
            System.out.println("1) Вывести все заметки");
            System.out.println("2) Создать новую заметку");
            System.out.println("3) Удалить заметку по номеру");
            System.out.println("4) Отредактировать заметку по номеру");
            System.out.println("5) Просмотреть заметку по номеру");
            System.out.println("6) Синхронизировать заметки с сервером");
            System.out.println("7) Автоматическая синхронизация с сервером [" + (autoSync ? "ON" : "OFF") + "]");
            System.out.println("8) Выйти из аккаунта");
            System.out.println("9) Завершить программу");
            if (!autoSync) {
                System.out.println("10) Просмотр заметок на сервере");
            }
            System.out.print("> ");

            switch (scanner.nextLine()) {
                case "1" -> showNotes(localNotes);
                case "2" -> createNote();
                case "3" -> deleteNote();
                case "4" -> editNote();
                case "5" -> viewNote(localNotes);
                case "6" -> syncNotesMenu();
                case "7" -> syncChange();
                case "8" -> logout();
                case "9" -> {
                    saveToFile();
                    System.exit(0);
                }
                case "10" -> {
                    if (autoSync) {
                        System.out.println("Неверный ввод. Попробуйте снова.");
                    } else {
                        serverNotesMenu();
                    }
                }
                default -> System.out.println("Неверный ввод. Попробуйте снова.");
            }
        }
    }

    private void serverNotesMenu() {
        loadNotesFromServer();
        while (true) {
            System.out.println("\nМеню серверных заметок");
            System.out.println("1) Вывести все заметки");
            System.out.println("2) Просмотреть заметку по номеру");
            System.out.println("3) Вернуться в главное меню");
            System.out.print("> ");

            switch (scanner.nextLine()) {
                case "1" -> showNotes(serverNotes);
                case "2" -> viewNote(serverNotes);
                case "3" -> {
                    return;
                }
                default -> System.out.println("Неверный ввод. Попробуйте снова.");
            }
        }



    }

    private void showNotes(List<Note> notes) {
        if (notes.isEmpty()) {
            System.out.println("Заметок нет.");
            return;
        }

        int index = 1;
        for (Note note : notes) {
            System.out.printf("%d) %s%n", index++, note.getTitle());
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
            System.out.print("> ");

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
                        Note newNote = new Note(currentUser, text, title);
                        if (autoSync) {
                            try {
                                httpPacket response = client.addNote(newNote);
                                if (response.isCorrect()) {
                                    System.out.println("Заметка сохранена на сервер.");
                                    localNotes.add(response.getNote());
                                    System.out.println("Заметка сохранена локально.");

                                } else {
                                    System.out.println("Ошибка: " + response.getBody());
                                }
                            } catch (Exception e) {
                                System.out.println("Ошибка подключения: " + e.getMessage());
                            }
                        } else {
                            localNotes.add(newNote);
                            System.out.println("Заметка сохранена локально.");
                        }
                        return;
                    }
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void deleteNote() {
        showNotes(localNotes);
        if (localNotes == null || localNotes.isEmpty()) return;

        while (true) {
            System.out.println("Введите номер заметки для удаления или 0 для возврата:");
            int num = readNumber(localNotes.size());
            if (num == 0) return;

            Note toDelete = localNotes.get(num - 1);
            System.out.printf("Удалить '%s'? (1 - Да, 2 - Нет)%n", toDelete.getTitle());
            String confirm = scanner.nextLine();

            if (confirm.equals("1")) {
                if (autoSync) {
                    try {
                        httpPacket response = client.deleteNoteById(currentUser.getId(), toDelete.getId());
                        if (response.isCorrect()) {
                            System.out.println("Заметка удалена с сервера.");
                        } else {
                            System.out.println("Ошибка: " + response.getBody());
                        }
                    } catch (Exception e) {
                        System.out.println("Ошибка подключения: " + e.getMessage());
                    }
                }
                localNotes.remove(toDelete);
                System.out.println("Заметка удалена локально.");
            } else {
                System.out.println("Удаление отменено.");
            }
        }
    }

    private void editNote() {
        showNotes(localNotes);
        if (localNotes == null || localNotes.isEmpty()) return;

        System.out.println("Введите номер заметки для редактирования или 0 для возврата:");
        int num = readNumber(localNotes.size());
        if (num == 0) return;

        Note original = localNotes.get(num - 1);
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
            System.out.print("> ");

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
                    if (autoSync) {
                        try {
                            httpPacket response = client.patchNote(edited);
                            if (response.isCorrect()) {
                                System.out.println("Заметка обновлена на сервере.");
                            } else {
                                System.out.println("Ошибка: " + response.getBody());
                            }
                        } catch (Exception e) {
                            System.out.println("Ошибка подключения: " + e.getMessage());
                        }
                    }
                    edited.setLastUpdateDate();
                    localNotes.set(num - 1, edited);
                    System.out.println("Заметка обновлена локально.");
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void viewNote(List<Note> notes) {
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

    private Map<Note, String> prepareSyncPlan() {
        Map<Note, String> plan = new HashMap<>();
        Map<Integer, Note> serverMap = serverNotes.stream()
                .collect(Collectors.toMap(Note::getId, note -> note));

        for (Note note : localNotes) {
            int id = note.getId();
            if (id == 0) {
                plan.put(note, "add");
            } else {
                Note serverNote = serverMap.get(id);
                if (!note.getText().equals(serverNote.getText()) || !note.getTitle().equals(serverNote.getTitle())) {
                    plan.put(note, "patch");
                    serverMap.remove(id);
                }
            }
        }
        return plan;
    }

    private void syncToServer(Map<Note, String> plan) {
        for (Map.Entry<Note, String> entry : plan.entrySet()) {
            try {
                switch (entry.getValue()) {
                    case "add" -> client.addNote(entry.getKey());
                    case "patch" -> client.patchNote(entry.getKey());
                }
            } catch (Exception e) {
                System.out.println("Ошибка синхронизации: " + e.getMessage());
            }
        }
    }

    private void syncNotesMenu() {
        System.out.println("\n1) Заменить локальные заметки серверными");
        System.out.println("2) Загрузить локальные заметки на сервер");
        System.out.println("0) Назад");
        System.out.print("> ");
        switch (scanner.nextLine()) {
            case "1" -> {
                loadNotesFromServer();
                localNotes = new ArrayList<>(serverNotes);
                System.out.println("Локальные заметки заменены серверными.");
            }
            case "2" -> {
                loadNotesFromServer();
                Map<Note, String> plan = prepareSyncPlan();
                syncToServer(plan);
                loadNotesFromServer();
                localNotes = new ArrayList<>(serverNotes);
                System.out.println("Синхронизация завершена.");
            }
            case "0" -> {
            }
            default -> System.out.println("Неверный ввод.");
        }
    }

    private void syncChange() {
        System.out.println("Автоматическая синхронизация с сервером ->" + (!autoSync ? "ВКЛЮЧЕНА" : "ВЫКЛЮЧЕНА"));
        autoSync = !autoSync;
    }

    private void saveToFile() {
        Map<String, Object> data = new HashMap<>();
        data.put("user", currentUser);
        data.put("notes", localNotes);
        try {
            mapper.writeValue(storage, data);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения данных.");
        }
    }

    private void loadFromFile() {
        if (!storage.exists()) return;
        try {
            Map<String, Object> data = mapper.readValue(storage, new TypeReference<>() {
            });
            currentUser = mapper.convertValue(data.get("user"), User.class);
            localNotes = mapper.convertValue(data.get("notes"), new TypeReference<>() {
            });
        } catch (IOException e) {
            System.out.println("Ошибка загрузки данных.");
        }
    }

    private void logout() {
        System.out.println("При выходе из аккаунта все изменения не синхронизированные с сервером будет потеряны!");
        System.out.print("Синхронизировать заметки с сервером перед выходом? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            syncNotesMenu();
        }
        currentUser = null;
        localNotes.clear();
        serverNotes.clear();
        saveToFile();
        startMenu();
    }

}
