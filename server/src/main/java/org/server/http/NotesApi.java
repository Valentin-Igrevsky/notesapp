package org.server.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.server.database.NoteRepository;
import org.server.models.Note;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.sql.*;

public class NotesApi implements HttpHandler {
    private final NoteRepository noteRepo = new NoteRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetNotes(exchange);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                handleCreateNote(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Server error");
        }
    }

    private void handleGetNotes(HttpExchange exchange) throws IOException {
        // Заглушка - в реальности нужно парсить параметры
        try {
            List<Note> notes = noteRepo.getUserNotes(1); // Фиксированный ID
            sendResponse(exchange, 200, notes.toString());
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Database error");
        }
    }

    private void handleCreateNote(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        Note note = new Note(); // Заглушка
        note.setUserId(1); // Фиксированный ID
        note.setTitle(body.split("\"")[3]); // Простейший парсинг
        note.setContent(body.split("\"")[7]);

        try {
            if (noteRepo.createNote(note)) {
                sendResponse(exchange, 201, "Note created");
            } else {
                sendResponse(exchange, 400, "Error creating note");
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Database error");
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String data) throws IOException {
        exchange.sendResponseHeaders(code, data.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data.getBytes());
        }
    }
}