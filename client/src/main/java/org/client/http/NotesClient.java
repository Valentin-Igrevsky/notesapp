package org.client.http;

import org.client.models.Note;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NotesClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> getNotes(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());


    }

    public HttpResponse<String> getNoteById(int userId, int noteId) throws Exception {
        String url = String.format("%s/data/get?uid=%s&id=%s", BASE_URL, userId, noteId);
        return getNotes(url);
    }

    public HttpResponse<String> getAllNotes(int userId) throws Exception {
        String url = String.format("%s/data/get/all?uid=%s", BASE_URL, userId);
        return getNotes(url);
    }

    public HttpResponse<String> addNote(Note note) throws Exception {
        String url = String.format("%s/data/new", BASE_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(note.toString()))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> deleteNoteById(int userId, int noteId) throws Exception {
        String url = String.format("%s/data/delete?uid=%s&id=%s", BASE_URL, userId, noteId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> patchNote(Note note) throws Exception {
        String url = String.format("%s/data/patch", BASE_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(note.toString()))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}