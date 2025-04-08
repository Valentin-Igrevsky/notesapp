package org.client.http;

import org.client.models.Note;
import org.client.utils.JsonUtils_client;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.lang.reflect.Type;
import java.util.List;
import com.google.gson.reflect.TypeToken;

public class NotesClient {
    private static final String BASE_URL = "http://localhost:8080/api/notes";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Note> getUserNotes(int userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?user_id=" + userId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        Type notesListType = new TypeToken<List<Note>>(){}.getType();
        return JsonUtils_client.fromJsonList(response.body(), notesListType);
    }

    public boolean createNote(Note note) throws Exception {
        String json = JsonUtils_client.toJson(note);
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