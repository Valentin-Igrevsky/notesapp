package org.client.http;

import org.client.models.Note;
import org.client.utils.NoteJsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class NotesClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client = HttpClient.newHttpClient();

    private Map.Entry<Integer, List<Note>> getNotes(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        int statusCode = response.statusCode();
        List<Note> serverNotes = null;

        if (statusCode == 200) {
            serverNotes = NoteJsonParser.fromJsonList(response.body(), Note.class);
        }

        return Map.entry(statusCode, serverNotes != null ? serverNotes : List.of());
    }

    public Map.Entry<Integer, List<Note>> getNoteById(int userId, int noteId) throws Exception {
        String url = String.format("%s/data/get?uid=%s&id=%s", BASE_URL, userId, noteId);
        return getNotes(url);
    }

    public Map.Entry<Integer, List<Note>> getAllNotes(int userId) throws Exception {
        String url = String.format("%s/data/get/all?uid=%s", BASE_URL, userId);
        return getNotes(url);
    }

    public Map.Entry<Integer, Note> addNote(Note note) throws Exception {
        String url = String.format("%s/data/new", BASE_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(note.toString()))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        int statusCode = response.statusCode();
        Note serverNote = null;

        if (statusCode == 200) {
            serverNote = NoteJsonParser.fromJson(response.body(), Note.class);
        }

        return Map.entry(statusCode, serverNote != null ? serverNote : new Note());
    }

    public Integer deleteNoteById(int userId, int noteId) throws Exception {
        String url = String.format("%s/data/delete?uid=%s&id=%s", BASE_URL, userId, noteId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        return response.statusCode();
    }

    public Integer patchNote(Note note) throws Exception {
        String url = String.format("%s/data/patch", BASE_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(note.toString()))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        return response.statusCode();
    }
}