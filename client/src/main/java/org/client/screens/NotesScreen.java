package org.client.screens;

import org.client.http.NotesClient;
import org.client.models.Note;
import java.util.List;
import java.util.Scanner;

public class NotesScreen {
    private final NotesClient notesClient = new NotesClient();
    private final Scanner scanner = new Scanner(System.in);
    private final int userId;

    public NotesScreen(int userId) {
        this.userId = userId;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== Мои заметки ===");
            System.out.println("1. Показать все");
            System.out.println("2. Создать");
            System.out.println("0. Назад");
            System.out.print("Выберите действие: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> showAllNotes();
                case 2 -> createNote();
                case 0 -> { return; }
                default -> System.out.println("Неверный ввод!");
            }
        }
    }

    private void showAllNotes() {
        try {
            List<Note> notes = notesClient.getUserNotes(userId);
            if (notes.isEmpty()) {
                System.out.println("Заметок нет");
                return;
            }

            System.out.println("\n=== Список заметок ===");
            for (Note note : notes) {
                System.out.println("ID: " + note.getId());
                System.out.println("Заголовок: " + note.getTitle());
                System.out.println("Текст: " + note.getContent());
                System.out.println("-------------------");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void createNote() {
        System.out.print("Заголовок: ");
        String title = scanner.nextLine();
        System.out.print("Текст: ");
        String content = scanner.nextLine();

        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(title);
        note.setContent(content);

        try {
            if (notesClient.createNote(note)) {
                System.out.println("Заметка создана!");
            } else {
                System.out.println("Ошибка создания");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}