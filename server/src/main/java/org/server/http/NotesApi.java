package org.server.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.server.database.NoteRepository;
import org.server.models.Note;
import org.server.Utils.JsonUtils_server;
import java.io.*;
import java.sql.SQLException; // Добавлен этот импорт
import java.util.List;

public class NotesApi implements HttpHandler {
    private final NoteRepository noteRepo = new NoteRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && path.endsWith("/notes")) {
                handleGetNotes(exchange);
            } else if ("POST".equals(method) && path.endsWith("/notes")) {
                handleCreateNote(exchange);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } catch (SQLException e) { // Теперь SQLException распознается
            sendResponse(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleGetNotes(HttpExchange exchange) throws IOException, SQLException {
        String query = exchange.getRequestURI().getQuery();
        int userId = Integer.parseInt(query.split("=")[1]);

        List<Note> notes = noteRepo.getUserNotes(userId);
        String response = JsonUtils_server.toJson(notes);
        sendResponse(exchange, 200, response);
    }

    private void handleCreateNote(HttpExchange exchange) throws IOException, SQLException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes());
        Note note = JsonUtils_server.fromJson(body, Note.class);

        if (noteRepo.createNote(note)) {
            sendResponse(exchange, 201, JsonUtils_server.toJson(note));
        } else {
            sendResponse(exchange, 400, "Error creating note");
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, data.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data.getBytes());
        }
    }
}