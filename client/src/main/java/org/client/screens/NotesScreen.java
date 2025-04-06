package org.client.screens;

import org.client.http.NotesClient;
import org.client.models.Note;
import java.util.List;
import java.util.Scanner;

public class NotesScreen {
    private final NotesClient notesClient;
    private final Scanner scanner;
    private final int userId;

    public NotesScreen(int userId) {
        this.notesClient = new NotesClient();
        this.scanner = new Scanner(System.in);
        this.userId = userId;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== Управление заметками ===");
            System.out.println("1. Показать все заметки");
            System.out.println("2. Создать заметку");
            System.out.println("0. Выйти");
            System.out.print("Выберите действие: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Очистка буфера

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
            List<Note> notes = notesClient.getAllNotes(userId);
            System.out.println("\n=== Ваши заметки ===");
            for (Note note : notes) {
                System.out.printf("%d. %s%n", note.getId(), note.getTitle());
                System.out.println(note.getContent());
                System.out.println("-------------------");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void createNote() {
        System.out.print("Заголовок: ");
        String title = scanner.nextLine();
        System.out.print("Содержание: ");
        String content = scanner.nextLine();

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);

        try {
            if (notesClient.createNote(note)) {
                System.out.println("Заметка создана!");
            } else {
                System.out.println("Ошибка создания!");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}