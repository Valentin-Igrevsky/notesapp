package org.client.http;

import org.client.models.Note;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class NotesClient {
    private static final String BASE_URL = "http://localhost:8080/notes";
    private final HttpClient client;

    public NotesClient() {
        this.client = HttpClient.newHttpClient();
    }

    public List<Note> getAllNotes(int userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?user_id=" + userId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        // Здесь должен быть парсинг JSON в List<Note>
        return List.of(); // Заглушка
    }

    public boolean createNote(Note note) throws Exception {
        String json = String.format(
                "{\"userId\":%d,\"title\":\"%s\",\"content\":\"%s\"}",
                note.getId(), note.getTitle(), note.getContent()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        return response.statusCode() == 201;
    }
}