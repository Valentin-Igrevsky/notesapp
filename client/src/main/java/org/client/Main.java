package org.client;

import org.client.models.User;
import org.client.screens.AuthScreen;
import org.client.screens.NotesScreen;

public class Main {
    public static void main(String[] args) {
        AuthScreen authScreen = new AuthScreen();
        User user = authScreen.show();

        if (user != null) {
            NotesScreen notesScreen = new NotesScreen(1); // Здесь должен быть реальный ID пользователя
            notesScreen.show();
        }
    }
}