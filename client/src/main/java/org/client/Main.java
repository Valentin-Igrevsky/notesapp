package org.client;

import org.client.screens.AuthScreen;
import org.client.screens.NotesScreen;

public class Main {
    public static void main(String[] args) {
        Integer userId = new AuthScreen().show();
        if (userId != null) {
            new NotesScreen(userId).show();
        }
    }
}